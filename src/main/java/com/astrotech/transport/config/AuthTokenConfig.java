package com.astrotech.transport.config;

import com.astrotech.transport.configProperties.AuthTokenProperties;
import com.astrotech.transport.core.GetSecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;

@RequiredArgsConstructor
@Configuration
public class AuthTokenConfig {
    private final AuthTokenProperties properties;

    @Bean("verifySecretKey")
    public SecretKey verifySecretKey() {
        return GetSecretKey.getKeys(properties.verifyEmailSecretKey());
    }

    @Bean("resetSecretKey")
    public SecretKey resetSecretKey() {
        return GetSecretKey.getKeys(properties.resetSecretKey());
    }

}
