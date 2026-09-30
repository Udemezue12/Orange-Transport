package com.astrotech.transport.configProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties(prefix = "spring.rabbitmq")
public record RabbitConfigProperties(
         String url,
        String host,

        String username,

        String password,

        int port,
         String VirtualHost
) {

}
