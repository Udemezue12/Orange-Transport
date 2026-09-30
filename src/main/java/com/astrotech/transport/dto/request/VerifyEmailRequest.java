package com.astrotech.transport.dto.request;

public record VerifyEmailRequest(
        String otp,
        String token
) {
}
