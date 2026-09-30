package com.astrotech.transport.config;

import com.astrotech.transport.configProperties.DojahProperties;

import com.astrotech.transport.configProperties.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {
    private final NotificationProperties notificationProperties;
    private final DojahProperties dojahProperties;
    private final JdkClientHttpRequestFactory getJdkClientHttpRequestFactory;

    @Bean("sharedRestClient")
    public RestClient sharedRestClient() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(20000);
        factory.setReadTimeout(10000);

        return RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    @Bean("termiiRestClient")
    public RestClient client() {

        return RestClient.builder()
                .requestFactory(getJdkClientHttpRequestFactory)
                .baseUrl(notificationProperties.termiiBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }



    @Bean("dojahRestClient")
    public RestClient driverLicenceRestClient() {
        return RestClient.builder()
                .baseUrl(dojahProperties.baseUrl())
                .defaultHeader("AppId", dojahProperties.appId())
                .defaultHeader("Authorization", dojahProperties.secretKey())
                .build();
    }
}
