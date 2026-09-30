package com.astrotech.transport.configProperties;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@ConfigurationProperties(prefix = "pingers")
public record PingerProperties(
    List<String> pingUrls){
}
