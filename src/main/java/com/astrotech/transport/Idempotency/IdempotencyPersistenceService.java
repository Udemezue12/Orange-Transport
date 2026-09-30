package com.astrotech.transport.Idempotency;

import com.astrotech.transport.entities.IdempotencyRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.astrotech.transport.enums.IdempotencyStatus;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.repositories.IdempotencyRecordRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyPersistenceService {

    private final IdempotencyRecordRepository repository;
    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProcessing(
            UUID key,
            String requestHash
    ) {

        var existing =
                repository.findByIdempotencyKey(key);

        if (existing.isPresent()) {

            var record = existing.get();

            validatePayload(
                    record,
                    requestHash);

            if (record.getStatus()
                    == IdempotencyStatus.PROCESSING) {

                throw new BadRequestException(
                        "Request is already being processed");
            }

            repository.delete(record);
        }

        var record = IdempotencyRecord.builder()
                .idempotencyKey(key)
                .requestHash(requestHash)
                .httpMethod(request.getMethod())
                .requestPath(request.getRequestURI())
                .status(IdempotencyStatus.PROCESSING)
                .lockedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        repository.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSuccess(
            UUID key,
            String requestHash,
            Object responseObject,
            String resourceId,
            String resourceType
    ) {

        var record = repository.findByIdempotencyKey(key)
                .orElseThrow(() ->
                        new BadRequestException("Idempotency record not found"));

        validatePayload(record, requestHash);

        Object body;
        Object headers = null;
        int statusCode = 200;
        String contentType = null;
        String responseClass = null;


        if (responseObject instanceof ResponseEntity<?> responseEntity) {

            body = responseEntity.getBody();
            headers = responseEntity.getHeaders();
            statusCode = responseEntity.getStatusCode().value();

            if (body != null) {
                responseClass = body.getClass().getName();
            }

            if (responseEntity.getHeaders().getContentType() != null) {
                contentType = responseEntity.getHeaders().getContentType().toString();
            }

        } else {

            body = responseObject;

            if (responseObject != null) {
                responseClass = responseObject.getClass().getName();
            }
        }

        record.setResponseBody(serialize(body));
        record.setResponseHeaders(serialize(headers));
        record.setResponseClass(responseClass);
        record.setStatusCode(statusCode);
        record.setContentType(contentType);

        record.setResourceId(resourceId);
        record.setResourceType(resourceType);

        record.setStatus(IdempotencyStatus.SUCCESS);
        record.setCompletedAt(Instant.now());

        repository.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailure(
            UUID key,
            String requestHash,
            Throwable exception
    ) {

        var record = repository
                .findByIdempotencyKey(key)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Idempotency record not found"));

        validatePayload(
                record,
                requestHash);

        record.setErrorMessage(
                exception.getMessage());

        record.setExceptionClass(
                exception.getClass().getName());

        record.setStatus(
                IdempotencyStatus.FAILED);

        record.setCompletedAt(
                Instant.now());

        repository.save(record);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteRecord(
            UUID key
    ) {

        repository.findByIdempotencyKey(key)
                .ifPresent(repository::delete);
    }

    private String serialize(
            Object object
    ) {

        if (object == null) {
            return null;
        }

        try {

            return objectMapper
                    .writeValueAsString(object);

        } catch (JsonProcessingException e) {

            log.error(
                    "Failed to serialize idempotent payload",
                    e);

            throw new BadRequestException(
                    "Failed to store idempotent response");
        }
    }

    private void validatePayload(
            IdempotencyRecord record,
            String requestHash
    ) {

        if (!record.getRequestHash()
                .equals(requestHash)) {

            throw new BadRequestException(
                    "Request payload does not match the original request");
        }
    }
}