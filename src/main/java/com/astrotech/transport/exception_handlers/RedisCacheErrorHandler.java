package com.astrotech.transport.exception_handlers;


import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedisCacheErrorHandler {
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {

            @Override
            public void handleCacheGetError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                log.warn(
                        "Redis GET failed for cache {} key {}",
                        cache.getName(),
                        key,
                        exception);

            }

            @Override
            public void handleCachePutError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key, @Nullable Object value) {
                log.warn(
                        "Redis PUT failed for cache {} key {}",
                        cache.getName(),
                        key,
                        exception);
            }

            @Override
            public void handleCacheEvictError(@NonNull RuntimeException exception, @NonNull Cache cache, @NonNull Object key) {
                log.warn(
                        "Redis DELETE failed for cache {} key {}",
                        cache.getName(),
                        key,
                        exception);

            }

            @Override
            public void handleCacheClearError(@NonNull RuntimeException exception, @NonNull Cache cache) {
                log.warn(
                        "Redis CLEAR failed for cache {}",
                        cache.getName(),
                        exception);

            }
        };
    }
}
