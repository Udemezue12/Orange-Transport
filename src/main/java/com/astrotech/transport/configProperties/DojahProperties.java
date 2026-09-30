package com.astrotech.transport.configProperties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "kyc-verification.dojah")
public record DojahProperties(
        String baseUrl,
        String appId,
        String secretKey

) {
}
