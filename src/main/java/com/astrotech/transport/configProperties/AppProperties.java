package com.astrotech.transport.configProperties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppProperties (
        Boolean enableWebAuthn,
        Boolean enableOAuth2,
        Boolean enableCors,
        Boolean enableCsrf,
        Boolean enableSmsToken,
        Boolean enableHttpOnly,
        Boolean httpSecure,
        Boolean useSimpleBroker,
        Boolean requestRegisterCodeForAdmins
){
}
