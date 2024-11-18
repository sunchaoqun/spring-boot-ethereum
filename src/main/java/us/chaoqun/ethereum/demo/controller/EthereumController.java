package us.chaoqun.ethereum.demo.controller;

import java.io.IOException;
import java.math.BigInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.DefaultBlockParameterName;

import us.chaoqun.ethereum.demo.model.TransactionRequest;
import us.chaoqun.ethereum.demo.model.TransactionResponse;
import us.chaoqun.ethereum.demo.service.EthereumService;
import us.chaoqun.ethereum.demo.util.ContractUtils;

@RestController
@RequestMapping("/api")
public class EthereumController {
    @Autowired
    private EthereumService ethereumService;
    
    @PostMapping("/transaction")
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        
        Web3j web3j = Web3j.build(new HttpService("https://sepolia.infura.io/v3/01563559eadd46efa145cc2cd225f72e"));

        BigInteger nonce = null;
        try {
            nonce = web3j.ethGetTransactionCount(
                    "0x2E59645ab79f11CD853871BEd5e21EbE2744640d", 
                    DefaultBlockParameterName.LATEST)
                    .send()
                    .getTransactionCount();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Current nonce: " + nonce);

        Long chainId = 11155111L;
        String encodedFunction = ContractUtils.encodeTransferData("0x659f594D2faF688d3357cAe13B8331B39674FC54", BigInteger.valueOf(1000000), BigInteger.ZERO, BigInteger.valueOf(chainId));

        TransactionRequest requestContract = new TransactionRequest();
        requestContract.setOperation("contract");
        requestContract.setContractAddress("0x0f1CCb955c68A07bd377EF43D3CBb7CC732ff377");  // 合约地址
        requestContract.setEncodedFunction(encodedFunction);  // 已编码的函数调用数据
        requestContract.setNonce(nonce.longValue());
        requestContract.setChainId(chainId);
        requestContract.setType(2);
        requestContract.setMaxFeePerGas(30000000000L);
        requestContract.setMaxPriorityFeePerGas(1500000000L);

        TransactionResponse response = ethereumService.createTransaction(requestContract);

        
        
        try {
            // 1. 首先发送交易
            String txHash = web3j.ethSendRawTransaction(response.getSignedTxPayload())
                .send()
                .getTransactionHash();
            
            // 2. 等待交易被打包（可选）
            TransactionReceipt receipt = web3j.ethGetTransactionReceipt(txHash)
                .send()
                .getResult();
                
            // 3. 调用合约的view方法来获取value
            
            System.out.println("Encoded function: " + encodedFunction);
            System.out.println("Contract address: " + requestContract.getContractAddress());
            
            EthCall ethCall = web3j.ethCall(
                Transaction.createEthCallTransaction(
                    "0x2E59645ab79f11CD853871BEd5e21EbE2744640d",
                    requestContract.getContractAddress(), 
                    encodedFunction
                ),
                DefaultBlockParameterName.LATEST
            ).send();
            
            if (ethCall.hasError()) {
                String errorMessage = ethCall.getError().getMessage();
                String revertReason = "";
                
                // 检查是否包含revert原因
                if (ethCall.getValue() != null && ethCall.getValue().length() >= 138) {
                    // 解码revert原因
                    String hexString = ethCall.getValue().substring(138);
                    byte[] bytes = org.web3j.utils.Numeric.hexStringToByteArray(hexString);
                    revertReason = new String(bytes);
                }
                
                System.out.println("Error: " + errorMessage);
                System.out.println("Revert reason: " + revertReason);
                System.out.println("Raw response: " + ethCall.getValue());
            }
            
            System.out.println("ethCall: " + ethCall.getValue());

            // 4. 解码返回值
            // String value = ContractUtils.decodeValueResult(ethCall.getValue());
            // System.out.println("Contract value: " + value);  // 打印到控制台
            
            // 5. 将结果添加到response中返回给客户端
            
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(response);
    }
}
