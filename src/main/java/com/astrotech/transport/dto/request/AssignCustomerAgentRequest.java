package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignCustomerAgentRequest(

        @NotNull(message = "UserId is required")
        UUID userId
) {
}
