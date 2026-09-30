package com.astrotech.transport.configProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "notification")
public record NotificationProperties(
        String termiiBaseUrl,
        String termiiApiKey,
        String termiiSenderId,

        String sendChampBaseUrl,
        String sendChampApiKey,
        String sendChampSenderId,


        String projectName,
        String uniqueName,

        String brevoApiKey,
        String brevoUrl,

        String emailHost,
        Integer emailPort,
        String emailUsername,
        String emailPassword,
        Boolean emailUseTls,

        String frontendUrl
) {


}
