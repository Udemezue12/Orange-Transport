package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TripCodeRequest(
        @NotBlank(message = "Trip code is required")
        String tripCode
) {
}
