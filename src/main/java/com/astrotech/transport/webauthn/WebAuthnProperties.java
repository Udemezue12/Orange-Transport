package com.astrotech.transport.webauthn;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

@ConfigurationProperties(prefix = "app.webauthn")
public record WebAuthnProperties(
        String rpId,
        String rpName,
        Set<String> webAuthnAllowedOrigins

) {
}
