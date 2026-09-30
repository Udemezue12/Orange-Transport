package com.astrotech.transport.service;

import com.astrotech.transport.enums.JwtType;
import com.astrotech.transport.events.BlacklistedTokenEvent;
import com.astrotech.transport.entities.BlacklistedToken;
import com.astrotech.transport.repositories.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistedTokenService {
    private final BlacklistedTokenRepository repository;
    private final ApplicationEventPublisher publisher;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX = "blacklisted:";


    public boolean isBlacklisted(String jti) {


        try {

            var exists =
                    redisTemplate.hasKey(PREFIX + jti);

            if (exists) {
                return true;
            }

        } catch (Exception e) {


            log.error(
                    "Redis unavailable during blacklist check: {}",
                    e.getMessage());
        }


        var dbExists =
                repository.existsByJti(jti);


        if (dbExists) {

            try {

                redisTemplate.opsForValue().set(
                        PREFIX + jti,
                        true,
                        Duration.ofDays(7));

            } catch (Exception e) {

                log.error(
                        "Redis unavailable during cache rehydration: {}",
                        e.getMessage());
            }
        }

        return dbExists;
    }

    @Transactional
    public BlacklistedToken blacklist(String token, JwtType tokenType, String jti, String userId, Date expiration) {

        if (token == null) {
            return null;
        }

        var convertedTime = convert(expiration);

        if (repository.existsByJti(jti)) {
            return null;
        }

        var blacklistedToken = repository.save(
                BlacklistedToken.builder()
                        .userId(userId)
                        .jti(jti)
                        .tokenType(tokenType)
                        .expiresAt(convertedTime)
                        .build());
        publisher.publishEvent(
                new BlacklistedTokenEvent(
                        jti,
                        expiration
                )
        );
        return blacklistedToken;
    }

    @Transactional
    public void cleanupExpiredTokens() {


        repository.deleteAllTokens();

        System.out.println(
                "Expired revoked tokens cleaned");
    }

    private OffsetDateTime convert(Date date) {
        return date.toInstant()
                .atOffset(ZoneOffset.UTC);
    }


}