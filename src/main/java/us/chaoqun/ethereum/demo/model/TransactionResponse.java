package us.chaoqun.ethereum.demo.model;

import lombok.Data;

@Data
public class TransactionResponse {
    private String ethChecksumAddress;  // for status operation
    private String signedTxHash;        // for sign operation
    private String signedTxPayload;     // for sign operation
    private String error;               // for error cases
} 