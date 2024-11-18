package us.chaoqun.ethereum.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "ethereum")
@Getter
@Setter
public class EthereumConfig {
    private String kmsKeyId;
    private String ethNetwork;
    
    // Validation
    @PostConstruct
    public void validate() {
        if (!StringUtils.hasLength(kmsKeyId)) {
            throw new IllegalStateException("KMS_KEY_ID must be configured");
        }
    }
} 