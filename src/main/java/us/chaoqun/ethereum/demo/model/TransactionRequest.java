package us.chaoqun.ethereum.demo.model;

import lombok.Data;

@Data
public class TransactionRequest {
    private String operation;     // "status" or "sign"
    private String dstAddress;    // destination address
    private Double amount;          // transaction amount
    private Long nonce;           // transaction nonce
    private Long chainId;         // optional
    private Integer type;         // optional
    private Long maxFeePerGas;    // optional
    private Long maxPriorityFeePerGas; // optional
} 