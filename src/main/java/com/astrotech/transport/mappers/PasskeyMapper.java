package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.PasskeyResponse;

import java.time.Instant;

public class PasskeyMapper {
    public static PasskeyResponse passkeyResponse(String id,
                                                  String label,
                                                  Instant createdAt,
                                                  Instant lastUsedAt) {
        return new PasskeyResponse(
                id,
                label,
                createdAt,
                lastUsedAt
        );
    }
}
