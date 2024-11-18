package us.chaoqun.ethereum.demo.service;

import java.math.BigInteger;
import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import us.chaoqun.ethereum.demo.model.SignedTransaction;
import us.chaoqun.ethereum.demo.model.TransactionParameters;
import us.chaoqun.ethereum.demo.model.TransactionRequest;
import us.chaoqun.ethereum.demo.model.TransactionResponse;
import us.chaoqun.ethereum.demo.util.EthereumUtils;

@Service
public class EthereumService {
    
    @Autowired
    private EthereumUtils ethereumUtils;
    
    @Value("${kms.key.id}")
    private String kmsKeyId;

    public TransactionResponse createTransaction(TransactionRequest request) {
        TransactionResponse response = new TransactionResponse();
        
        switch (request.getOperation()) {
            case "status":
                byte[] pubKey = ethereumUtils.getKmsPublicKey(kmsKeyId);
                String ethAddress = ethereumUtils.calcEthAddress(pubKey);
                response.setEthChecksumAddress(ethAddress);
                break;
                
            case "sign":
                if (request.getDstAddress() == null || 
                    request.getAmount() == null || 
                    request.getAmount() < 0 || 
                    request.getNonce() == null || 
                    request.getNonce() < 0) {
                    response.setError("missing parameter - sign requires amount, dstAddress and nonce to be specified");
                    return response;
                }
                
                BigDecimal amount = BigDecimal.valueOf(request.getAmount());
                
                TransactionParameters txParams = ethereumUtils.getTransactionParameters(
                    request.getDstAddress(),
                    amount,
                    BigInteger.valueOf(request.getNonce()),
                    request.getChainId(),
                    request.getType(),
                    BigInteger.valueOf(request.getMaxFeePerGas()),
                    BigInteger.valueOf(request.getMaxPriorityFeePerGas())
                );
                
                byte[] pubKeyForSign = ethereumUtils.getKmsPublicKey(kmsKeyId);
                String ethChecksumAddr = ethereumUtils.calcEthAddress(pubKeyForSign);
                
                SignedTransaction signedTx = ethereumUtils.assembleTx(
                    txParams,
                    kmsKeyId,
                    ethChecksumAddr,
                    request.getChainId()
                );
                
                response.setSignedTxHash(signedTx.getTxHash());
                response.setSignedTxPayload(signedTx.getEncodedTransaction());
                break;
        }
        
        return response;
    }
}