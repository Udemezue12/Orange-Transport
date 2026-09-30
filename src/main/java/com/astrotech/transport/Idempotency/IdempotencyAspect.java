package com.astrotech.transport.Idempotency;

import com.astrotech.transport.entities.IdempotencyRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.repositories.IdempotencyRecordRepository;
import com.astrotech.transport.utilities.hash.RequestHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class IdempotencyAspect {

    private final HttpServletRequest request;
    private final IdempotencyRecordRepository idemRepo;
    private final IdempotencyPersistenceService idemPersist;
    private final RequestHashUtil hashUtil;
    private final RedisIdempotency redisIdem;
    private final ObjectMapper objectMapper;

    @Around("@annotation(idempotent)")
    public Object handle(
            ProceedingJoinPoint joinPoint,
            Idempotent idempotent
    ) throws Throwable {

        var idemKey = request.getHeader("Idempotency-Key");

        if (idemKey == null || idemKey.isBlank()) {
            throw new BadRequestException(
                    "Missing Idempotency-Key header");
        }

        UUID uuidKey;

        try {
            uuidKey = UUID.fromString(idemKey);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid Idempotency-Key format");
        }

        var requestHash = hashUtil.hash(
                joinPoint.getArgs());

        var existing = idemRepo.findByIdempotencyKey(
                uuidKey);

        if (existing.isPresent()) {

            IdempotencyRecord record = existing.get();

            validatePayload(
                    record,
                    requestHash);

            switch (record.getStatus()) {

                case SUCCESS:
                    return replayResponse(record);

                case PROCESSING:
                    throw new BadRequestException(
                            "Request is already being processed");

                case FAILED:

                    log.info(
                            "Retrying previously failed idempotent request. key={}",
                            uuidKey);

                    idemPersist.deleteRecord(uuidKey);
                    break;
            }
        }

        return redisIdem.runOnce(
                idempotent.prefix() + ":" + idemKey,
                idempotent.ttl(),
                () -> processRequest(
                        joinPoint,
                        uuidKey,
                        requestHash)
        );
    }

    private Object processRequest(
            ProceedingJoinPoint joinPoint,
            UUID uuidKey,
            String requestHash) {

        idemPersist.saveProcessing(
                uuidKey,
                requestHash);

        try {

            var response = joinPoint.proceed();

            var body = response;


            if (response instanceof ResponseEntity<?> responseEntity) {
                body = responseEntity.getBody();
            }


            var resourceId = IdempotentResourceExtractor.extractId(body);
            var resourceType = IdempotentResourceExtractor.extractType(body);

            idemPersist.saveSuccess(
                    uuidKey,
                    requestHash,
                    response,
                    resourceId,
                    resourceType
            );

            return response;

        } catch (Throwable e) {

            idemPersist.saveFailure(
                    uuidKey,
                    requestHash,
                    e);

            if (e instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }

            throw new RuntimeException(e);
        }
    }

    private void validatePayload(
            IdempotencyRecord record,
            String requestHash
    ) {

        if (!record.getRequestHash()
                .equals(requestHash)) {

            throw new BadRequestException(
                    "Idempotency key reused with different payload");
        }
    }

    private Object replayResponse(
            IdempotencyRecord record
    ) {

        if (record.getResponseBody() == null) {

            throw new BadRequestException(
                    "Stored idempotent response is missing");
        }

        try {

            var body = objectMapper.readValue(
                    record.getResponseBody(),
                    Object.class);

            return ResponseEntity
                    .status(record.getStatusCode())
                    .header(
                            "X-Idempotent-Replay",
                            "true")
                    .body(body);

        } catch (Exception e) {

            log.error(
                    "Failed to replay idempotent response",
                    e);

            throw new BadRequestException(
                    "Unable to replay stored response");
        }
    }

    private static class IdempotentResourceExtractor {

        public static String extractId(Object obj) {
            if (obj == null) return null;


            try {
                var method = obj.getClass().getMethod("getId");
                Object value = method.invoke(obj);
                if (value != null) return value.toString();
            } catch (Exception ignored) {
            }


            try {
                var field = obj.getClass().getDeclaredField("id");
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) return value.toString();
            } catch (Exception ignored) {
            }


            try {
                var field = obj.getClass().getDeclaredField("reference");
                field.setAccessible(true);
                Object value = field.get(obj);
                if (value != null) return value.toString();
            } catch (Exception ignored) {
            }

            return null;
        }

        public static String extractType(Object obj) {
            return obj == null
                    ? null
                    : obj.getClass().getSimpleName().toUpperCase();
        }
    }
}