package com.astrotech.transport.responseBuilder;

import lombok.Builder;
import org.springframework.http.ResponseEntity;

import java.time.Instant;


public record ApiErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {


}
