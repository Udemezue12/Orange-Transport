package com.astrotech.transport.configProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "auth")
public record AuthTokenProperties(
        String redisUrl,

        String resetPasswordSalt,

        String resetSecretKey,

        String verifyEmailSalt,

        String verifyEmailSecretKey

) {




}
