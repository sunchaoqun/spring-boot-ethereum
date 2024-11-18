package us.chaoqun.ethereum.demo.util;

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.GetPublicKeyRequest;
import com.amazonaws.services.kms.model.SignRequest;
import org.bouncycastle.asn1.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import us.chaoqun.ethereum.demo.model.SignatureData;
import us.chaoqun.ethereum.demo.model.SignedTransaction;
import us.chaoqun.ethereum.demo.model.TransactionParameters;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.Arrays;

@Component
public class EthereumUtils {
    private static final BigInteger SECP256_K1_N = new BigInteger("fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141", 16);
    private final AWSKMS kmsClient;
    private final Web3j web3j;

    public EthereumUtils(@Value("${ethereum.node.url}") String nodeUrl) {
        this.kmsClient = AWSKMSClientBuilder.standard().build();
        this.web3j = Web3j.build(new HttpService(nodeUrl));
    }

    public byte[] getKmsPublicKey(String keyId) {
        GetPublicKeyRequest request = new GetPublicKeyRequest().withKeyId(keyId);
        return kmsClient.getPublicKey(request).getPublicKey().array();
    }

    public String calcEthAddress(byte[] pubKey) {
        try {
            // Decode ASN.1 structure
            ASN1InputStream asn1Stream = new ASN1InputStream(pubKey);
            DLSequence sequence = (DLSequence) asn1Stream.readObject();
            DERBitString publicKeyBitString = (DERBitString) sequence.getObjectAt(1);
            byte[] publicKeyBytes = publicKeyBitString.getBytes();
            
            // Remove the first byte (compression prefix) from the actual public key
            byte[] pubKeyNoPrefix = Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length);
            
            // Calculate Keccak-256 hash and convert to address
            byte[] addressBytes = Hash.sha3(pubKeyNoPrefix);
            String address = Numeric.toHexString(addressBytes);
            // Take last 40 characters (20 bytes)
            address = address.substring(address.length() - 40);
            // Add "0x" prefix and convert to checksum address
            return Keys.toChecksumAddress("0x" + address);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate Ethereum address", e);
        }
    }

    public SignatureData findEthSignature(String keyId, byte[] messageHash) {
        // 确保消息摘要是32字节长度
        if (messageHash.length != 32) {
            throw new IllegalArgumentException("Message hash must be exactly 32 bytes long");
        }

        SignRequest request = new SignRequest()
            .withKeyId(keyId)
            .withMessage(ByteBuffer.wrap(messageHash))
            .withSigningAlgorithm("ECDSA_SHA_256")
            .withMessageType("DIGEST");

        byte[] signatureBytes = kmsClient.sign(request).getSignature().array();
        
        try {
            // Decode ASN.1 signature (equivalent to Python's asn1tools.compile_string)
            ASN1InputStream asn1Stream = new ASN1InputStream(signatureBytes);
            DLSequence sequence = (DLSequence) asn1Stream.readObject();
            
            // Extract r and s values from the ASN.1 sequence
            BigInteger r = ((ASN1Integer) sequence.getObjectAt(0)).getPositiveValue();
            BigInteger s = ((ASN1Integer) sequence.getObjectAt(1)).getPositiveValue();
            
            // Handle s value like in Python code
            BigInteger secp256k1NHalf = SECP256_K1_N.divide(BigInteger.valueOf(2));
            if (s.compareTo(secp256k1NHalf) > 0) {
                s = SECP256_K1_N.subtract(s);
            }
            
            asn1Stream.close();
            return new SignatureData(r, s);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to decode ASN.1 signature", e);
        }
    }

    public TransactionParameters getTransactionParameters(String dstAddress, 
                                                        BigDecimal amount, 
                                                        BigInteger nonce,
                                                        long chainId,
                                                        int type,
                                                        BigInteger maxFeePerGas,
                                                        BigInteger maxPriorityFeePerGas) {
        // 将 amount 从 ETH 转换为 Wei
        BigInteger valueInWei = Convert.toWei(String.valueOf(amount.doubleValue()), Convert.Unit.ETHER).toBigInteger();
        
        System.out.println("Amount in ETH: " + amount);
        System.out.println("Amount in Wei: " + valueInWei);
        
        // 添加调试信息
        System.out.println("Gas Limit: " + BigInteger.valueOf(160000));
        System.out.println("Max Fee Per Gas: " + maxFeePerGas + " wei");
        System.out.println("Max Priority Fee Per Gas: " + maxPriorityFeePerGas + " wei");
        System.out.println("Estimated total gas cost: " + 
            BigInteger.valueOf(160000).multiply(maxFeePerGas).add(valueInWei) + " wei");
        
        return TransactionParameters.builder()
                .nonce(nonce)
                .to(dstAddress)
                .value(valueInWei)
                .data("0x00")
                .gasLimit(BigInteger.valueOf(160000))
                .maxFeePerGas(maxFeePerGas)
                .maxPriorityFeePerGas(maxPriorityFeePerGas)
                .type(type)
                .chainId(chainId)
                .build();
    }

    private byte findRecoveryId(byte[] messageHash, SignatureData signature, String ethChecksumAddr, long chainId) {
        System.out.println("Chain ID: " + chainId);
        System.out.println("Target address: " + ethChecksumAddr);
        
        for (int i = 0; i < 4; i++) {
            try {
                ECDSASignature ecdsaSignature = new ECDSASignature(signature.getR(), signature.getS());
                BigInteger publicKey = Sign.recoverFromSignature(
                    i,
                    ecdsaSignature,
                    messageHash
                );
                
                if (publicKey != null) {
                    String recoveredAddress = "0x" + Keys.getAddress(publicKey);
                    System.out.println("Recovery attempt " + i + " recovered address: " + recoveredAddress);
                    
                    if (ethChecksumAddr.toLowerCase().equals(recoveredAddress.toLowerCase())) {
                        // 对于 EIP-1559，使用 27 + recId
                        byte v = (byte)(27 + i);
                        System.out.println("Using standard v value: 27 + " + i + " = " + v);
                        return v;
                    }
                }
            } catch (Exception e) {
                System.out.println("Failed to recover with recId " + i + ": " + e.getMessage());
            }
        }
        
        throw new RuntimeException("Could not recover public key from signature. Target address: " + ethChecksumAddr);
    }

    public SignedTransaction assembleTx(TransactionParameters txParams, 
                                      String keyId,
                                      String ethChecksumAddr,
                                      long chainId) {
        // 在发送交易前调用
        BigInteger balance = new BigInteger("0");
        BigInteger nonce = new BigInteger("0");
        try {
            balance = checkBalance(ethChecksumAddr);
            nonce = getNonce(ethChecksumAddr);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 计算总成本
        BigInteger totalCost = txParams.getGasLimit()
                .multiply(txParams.getMaxFeePerGas())
                .add(txParams.getValue());

        if (balance.compareTo(totalCost) < 0) {
            throw new IllegalStateException(String.format(
                "Insufficient balance. Required: %s wei, Available: %s wei",
                totalCost,
                balance
            ));
        }
        
        // Create RawTransaction
        RawTransaction rawTransaction = RawTransaction.createTransaction(
            chainId,
            txParams.getNonce(),
            txParams.getGasLimit(),
            txParams.getTo(),
            txParams.getValue(),
            txParams.getData(),
            txParams.getMaxPriorityFeePerGas(),
            txParams.getMaxFeePerGas()
        );

        // Get the transaction hash that needs to be signed
        byte[] messageHash = TransactionEncoder.encode(rawTransaction);
        System.out.println("Raw Message: " + Numeric.toHexString(messageHash));
        
        // Hash the encoded transaction
        messageHash = Hash.sha3(messageHash);
        System.out.println("Message Hash: " + Numeric.toHexString(messageHash));
        
        SignatureData signature = findEthSignature(keyId, messageHash);
        
        // Debug output
        System.out.println("Signature R: " + Numeric.toHexString(Numeric.toBytesPadded(signature.getR(), 32)));
        System.out.println("Signature S: " + Numeric.toHexString(Numeric.toBytesPadded(signature.getS(), 32)));
        
        // Find recovery id and encode transaction
        byte v = findRecoveryId(messageHash, signature, ethChecksumAddr, chainId);
        System.out.println("Final v value: " + v);
        
        Sign.SignatureData web3jSignature = new Sign.SignatureData(
            v,
            Numeric.toBytesPadded(signature.getR(), 32),
            Numeric.toBytesPadded(signature.getS(), 32)
        );
        
        byte[] encodedTransaction = TransactionEncoder.encode(rawTransaction, web3jSignature);
        String txHash = Numeric.toHexString(Hash.sha3(encodedTransaction));

        return new SignedTransaction(txHash, Numeric.toHexString(encodedTransaction));
    }

    public BigInteger checkBalance(String address) throws IOException {
        BigInteger balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)
                .send()
                .getBalance();
        System.out.println("Account Balance in Wei: " + balance);
        System.out.println("Account Balance in ETH: " + Convert.fromWei(balance.toString(), Convert.Unit.ETHER));
        return balance;
    }

    public BigInteger getNonce(String address) throws IOException {
        BigInteger nonce = web3j.ethGetTransactionCount(
                address, 
                DefaultBlockParameterName.LATEST)
                .send()
                .getTransactionCount();
        System.out.println("Current nonce: " + nonce);
        return nonce;
    }
} 