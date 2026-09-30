package com.astrotech.transport.responseBuilder;

import java.time.Instant;

public class ApiErrorResponseMapper {
    public static ApiErrorResponse createError(int status, String error, String message, String path) {
        
        return new ApiErrorResponse(
                status,
                error,
                message,
                path,
                Instant.now()
        );

    }
}
