package com.astrotech.transport.configProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "app.stomp")
public record StompRabbitBrokerProperties (
        int relayPort
){





}
