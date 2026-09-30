package com.astrotech.transport.config;

import com.astrotech.transport.configProperties.DojahProperties;
import com.astrotech.transport.configProperties.NotificationProperties;
import com.astrotech.transport.configProperties.PaymentProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {
    private final PaymentProperties properties;
    private final NotificationProperties notificationProperties;
    private final DojahProperties dojahProperties;

    @Bean("paystackWebClient")
    public WebClient paystackWebClient() {
        return buildClient(properties.paystackBaseUrl(), properties.paystackSecretKey());
    }
    @Bean("monnifyWebClient")
    public WebClient monnifyWebClient() {
        return WebClient.builder()
                .baseUrl(properties.monnifyBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


    @Bean("flutterwaveWebClient")
    public WebClient flutterwaveWebClient() {
        return buildClient(properties.flutterwaveBaseUrl(), properties.flutterwaveSecretKey());
    }

    @Bean("fileHashWebClient")
    public WebClient fileHashWebClient() {
        return WebClient.builder()
                .build();
    }

    @Bean("brevoWebClient")
    public WebClient brevoWebClient() {
        return WebClient.builder()
                .baseUrl(notificationProperties.brevoUrl())
                .defaultHeader("api-key", notificationProperties.brevoApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("termiiWebClient")
    public WebClient client() {

        return WebClient.builder()
                .baseUrl(notificationProperties.termiiBaseUrl())
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    @Bean("dojahWebClient")
    public WebClient dojahClient() {
        return WebClient.builder()
                .baseUrl(dojahProperties.baseUrl())
                .defaultHeader("AppId", dojahProperties.appId())
                .defaultHeader("Authorization", dojahProperties.secretKey())
                .build();
    }

    @Bean("sendChampWebClient")
    public WebClient sendChampWebClient() {
        return WebClient.builder()
                .baseUrl(notificationProperties.sendChampBaseUrl())
                .defaultCookie(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + notificationProperties.sendChampApiKey())
                .build();
    }


    private WebClient buildClient(String baseUrl, String secretKey) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

}
