package com.astrotech.transport.configProperties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.terminal")
public record ZoneIdProperties(
        String zone
) {
}
