package us.chaoqun.ethereum.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignedTransaction {
    private String txHash;
    private String encodedTransaction;
} 