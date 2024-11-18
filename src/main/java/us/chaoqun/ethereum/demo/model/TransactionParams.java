package us.chaoqun.ethereum.demo.model;
import java.math.BigInteger;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionParams {
    private String destinationAddress;
    private BigInteger amount;
    private BigInteger nonce;
    private BigInteger chainId;
    private BigInteger type;
    private BigInteger maxFeePerGas;
    private BigInteger maxPriorityFeePerGas;
} 