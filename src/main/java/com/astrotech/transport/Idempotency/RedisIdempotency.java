package com.astrotech.transport.Idempotency;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class RedisIdempotency {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "idempotency:";

    public String acquire(
            String key,
            long ttlSeconds) {

        String token = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(
                        PREFIX + key,
                        token,
                        Duration.ofSeconds(ttlSeconds));

        return Boolean.TRUE.equals(success)
                ? token
                : null;
    }

    public void release(
            String key,
            String token) {

        String script = """
                if redis.call('get', KEYS[1]) == ARGV[1]
                then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();

        redisScript.setScriptText(script);
        redisScript.setResultType(Long.class);

        redisTemplate.execute(
                redisScript,
                Collections.singletonList(
                        PREFIX + key),
                token);
    }

    public <T> T runOnce(
            String key,
            long ttlSeconds,
            Supplier<T> action) {

        String token = acquire(key, ttlSeconds);

        if (token == null) {
            throw new RuntimeException(
                    "Duplicate request in progress");
        }

        try {
            return action.get();
        } finally {
            release(key, token);
        }
    }
}
