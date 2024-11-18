package us.chaoqun.ethereum.demo.model;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignatureData {
    private BigInteger r;
    private BigInteger s;
} 