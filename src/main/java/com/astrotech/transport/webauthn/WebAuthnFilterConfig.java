package com.astrotech.transport.webauthn;


import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import org.springframework.security.web.webauthn.authentication.*;
import org.springframework.security.web.webauthn.management.*;
import org.springframework.security.web.webauthn.registration.*;

import static com.astrotech.transport.webauthn.WebAuthnEndpoints.*;


@Configuration
public class WebAuthnFilterConfig {

    @Bean
    public PublicKeyCredentialCreationOptionsFilter passkeyRegistrationOptionsFilter(WebAuthnRelyingPartyOperations relyingPartyOperations, PublicKeyCredentialCreationOptionsRepository optionsRepository) {
        var filter = new PublicKeyCredentialCreationOptionsFilter(relyingPartyOperations);
        filter.setRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(
                        HttpMethod.POST,
                        PASSKEY_REGISTER_OPTIONS

                )
        );

        filter.setCreationOptionsRepository(
                optionsRepository
        );

        return filter;

    }

    @Bean
    public WebAuthnRegistrationFilter
    passkeyRegistrationFilter(
            JdbcUserCredentialRepository credentialRepository,
            WebAuthnRelyingPartyOperations relyingPartyOperations,
            PublicKeyCredentialCreationOptionsRepository optionsRepository
    ) {

        var filter =
                new WebAuthnRegistrationFilter(
                        credentialRepository,
                        relyingPartyOperations
                );

        filter.setRegisterCredentialMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST,
                        PASSKEY_REGISTER_VERIFY
                )
        );

        filter.setCreationOptionsRepository(
                optionsRepository
        );

        return filter;
    }

    @Bean
    public PublicKeyCredentialRequestOptionsFilter
    passkeyAuthenticationOptionsFilter(
            WebAuthnRelyingPartyOperations relyingPartyOperations,
            PublicKeyCredentialRequestOptionsRepository optionsRepository
    ) {

        var filter =
                new PublicKeyCredentialRequestOptionsFilter(
                        relyingPartyOperations
                );

        filter.setRequestMatcher(
                PathPatternRequestMatcher.withDefaults().matcher(
                        HttpMethod.POST,
                        PASSKEY_LOGIN_OPTIONS
                )
        );

        filter.setRequestOptionsRepository(
                optionsRepository
        );

        return filter;
    }

    @Bean
    public WebAuthnAuthenticationFilter
    passkeyAuthenticationFilter(
            @Lazy AuthenticationManager authenticationManager,
            PublicKeyCredentialRequestOptionsRepository optionsRepository,
            WebAuthnSuccessHandler webAuthnSuccessHandler,
            WebAuthnFailureHandler webAuthnFailureHandler
    ) {

        var filter = new WebAuthnAuthenticationFilter();

        filter.setFilterProcessesUrl(
                PASSKEY_LOGIN_VERIFY
        );

        filter.setRequestOptionsRepository(
                optionsRepository
        );

        filter.setAuthenticationManager(
                authenticationManager
        );
        filter.setAuthenticationSuccessHandler(
                webAuthnSuccessHandler
        );
        filter.setAuthenticationFailureHandler(webAuthnFailureHandler);

        return filter;
    }




}
