package com.astrotech.transport.config;

import com.astrotech.transport.core.GetSecretKey;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Data
public class JwtConfig {
    private String secret;
    private Long accessExpiration;
    private Long refreshExpiration;
    private String issuer;


    private String signingPrivateKey;
    private String signingPublicKey;


    private String encryptionPrivateKey;
    private String encryptionPublicKey;

    public SecretKey getSecretKey() {
        return GetSecretKey.getKeys(secret);

    }
}
