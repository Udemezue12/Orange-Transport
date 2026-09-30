package com.astrotech.transport.webauthn;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.webauthn.authentication.HttpSessionPublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.authentication.PublicKeyCredentialRequestOptionsRepository;
import org.springframework.security.web.webauthn.registration.HttpSessionPublicKeyCredentialCreationOptionsRepository;
import org.springframework.security.web.webauthn.registration.PublicKeyCredentialCreationOptionsRepository;

@Configuration
public class WebAuthnChallengeConfig {

    @Bean
    public PublicKeyCredentialCreationOptionsRepository
    creationOptionsRepository() {

        return new HttpSessionPublicKeyCredentialCreationOptionsRepository();
    }

    @Bean
    public PublicKeyCredentialRequestOptionsRepository
    requestOptionsRepository() {

        return new HttpSessionPublicKeyCredentialRequestOptionsRepository();
    }
}
