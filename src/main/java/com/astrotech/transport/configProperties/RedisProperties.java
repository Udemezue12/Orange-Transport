package com.astrotech.transport.configProperties;


import org.springframework.boot.context.properties.ConfigurationProperties;




@ConfigurationProperties(prefix = "spring.data.redis")
public record RedisProperties(
        String url,
        String host,

        String username,

        String password,

        int port
) {

}
