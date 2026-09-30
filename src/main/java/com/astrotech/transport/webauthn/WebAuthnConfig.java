package com.astrotech.transport.webauthn;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.web.webauthn.api.PublicKeyCredentialRpEntity;
import org.springframework.security.web.webauthn.management.JdbcPublicKeyCredentialUserEntityRepository;
import org.springframework.security.web.webauthn.management.JdbcUserCredentialRepository;
import org.springframework.security.web.webauthn.management.WebAuthnRelyingPartyOperations;
import org.springframework.security.web.webauthn.management.Webauthn4JRelyingPartyOperations;



@Configuration
public class WebAuthnConfig {


    @Bean
    JdbcPublicKeyCredentialUserEntityRepository
    publicKeyCredentialUserEntityRepository(JdbcOperations jdbcOperations) {

        return new JdbcPublicKeyCredentialUserEntityRepository(jdbcOperations);
    }

    @Bean
    JdbcUserCredentialRepository
    userCredentialRepository(JdbcOperations jdbcOperations) {

        return new JdbcUserCredentialRepository(jdbcOperations);
    }

    @Bean
    PublicKeyCredentialRpEntity relyingParty(WebAuthnProperties webAuthnProperties) {

        return PublicKeyCredentialRpEntity.builder()
                .id(webAuthnProperties.rpId())
                .name(webAuthnProperties.rpName())
                .build();
    }

    @Bean
    WebAuthnRelyingPartyOperations webAuthnRelyingPartyOperations(
            JdbcPublicKeyCredentialUserEntityRepository userEntities,
            JdbcUserCredentialRepository userCredentials,
            PublicKeyCredentialRpEntity relyingParty,
            WebAuthnProperties webAuthnProperties
            ) {

        return new Webauthn4JRelyingPartyOperations(
                userEntities,
                userCredentials,
                relyingParty,
                webAuthnProperties.webAuthnAllowedOrigins()
        );
    }
}
