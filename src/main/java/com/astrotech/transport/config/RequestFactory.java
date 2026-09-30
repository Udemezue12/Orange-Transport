package com.astrotech.transport.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RequestFactory {

    @Bean
    public HttpClient getHttpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Bean
    public JdkClientHttpRequestFactory getJdkClientHttpRequestFactory() {
        var requestFactory = new JdkClientHttpRequestFactory(getHttpClient());
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        return requestFactory;
    }
}
