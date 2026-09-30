package com.astrotech.transport.configProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.encryption")
public record EncryptionProperties(
        String aesAlgorithm,
        int gcmTagLength,
        int ivLength,
        String rsaAlgorithm,
        String serverKeyHex,
        String serverSecret,
        boolean productionMode
) {

}
