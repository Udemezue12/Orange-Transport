package com.astrotech.transport.config;

import com.astrotech.transport.configProperties.RedisProperties;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class BucketConfig {


    @Bean
    public RedisClient redisClient(
            RedisProperties config) {

        return RedisClient.create(config.url());
    }

    @Bean
    public LettuceBasedProxyManager proxyManager(
            RedisClient redisClient) {

        return LettuceBasedProxyManager.builderFor(redisClient).
                withExpirationStrategy(ExpirationAfterWriteStrategy
                        .basedOnTimeForRefillingBucketUpToMax(
                                Duration.ofMinutes(10)
                        )).build();
    }

}
