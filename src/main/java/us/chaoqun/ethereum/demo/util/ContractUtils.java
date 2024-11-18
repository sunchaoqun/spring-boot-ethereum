package us.chaoqun.ethereum.demo.util;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.Utf8String;
public class ContractUtils {
    
    /**
     * 构建ERC20 transfer函数的编码数据
     */
    public static String encodeTransferData(String toAddress, BigInteger amount, BigInteger seq, BigInteger chain) {
        Function function = new Function(
            "transfer",  // 函数名
            Arrays.asList(
                new Address(toAddress),    // 接收地址
                new Uint256(amount),       // 转账金额
                new Uint256(seq),          // 序列号
                new Uint256(chain)         // 链ID
            ),
            Collections.emptyList()  // 返回类型，transfer没有返回值所以是空列表
        );
        
        return FunctionEncoder.encode(function);
    }
    
    /**
     * 构建任意合约函数的编码数据
     */
    public static String encodeFunctionData(
            String functionName,
            List<Type> parameters,
            List<TypeReference<?>> returnTypes) {
        Function function = new Function(functionName, parameters, returnTypes);
        return FunctionEncoder.encode(function);
    }
    
    /**
     * 构建读取HelloWorld合约value值的编码数据
     */
    public static String encodeGetValueData() {
        Function function = new Function(
            "value",  // 函数名，public变量会自动创建同名的getter函数
            Collections.emptyList(), // 没有输入参数
            Arrays.asList(new TypeReference<Utf8String>() {}) // 返回类型是string
        );
        
        return FunctionEncoder.encode(function);
    }

    public static String decodeValueResult(String value) {
        Function function = new Function(
            "value", 
            Collections.emptyList(),
            Arrays.asList(new TypeReference<Utf8String>() {})
        );
        
        List<Type> decoded = FunctionReturnDecoder.decode(
            value, 
            function.getOutputParameters()
        );
        
        if (decoded.size() > 0) {
            return ((Utf8String) decoded.get(0)).getValue();
        }
        return null;
    }
} 