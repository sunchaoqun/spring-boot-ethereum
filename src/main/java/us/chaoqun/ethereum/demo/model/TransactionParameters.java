package us.chaoqun.ethereum.demo.model;

import java.math.BigInteger;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionParameters {
    private BigInteger nonce;
    private String to;
    private BigInteger value;
    private String data;
    private BigInteger gasLimit;
    private BigInteger maxFeePerGas;
    private BigInteger maxPriorityFeePerGas;
    private int type;
    private long chainId;
} 