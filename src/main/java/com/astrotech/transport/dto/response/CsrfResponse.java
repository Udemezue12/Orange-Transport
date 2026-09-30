package com.astrotech.transport.dto.response;

public record CsrfResponse(
        String token,
        String header,
        String parameter
) {
}
