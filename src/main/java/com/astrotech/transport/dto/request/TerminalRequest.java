package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record TerminalRequest(
        @NotBlank(message = "Terminal Name is required")
        @Size(min = 2, max = 256, message = "Terminal Name should not be more than 256 characters")
        String terminalName,
        @NotBlank(message = "State is required")
        @Size(min = 2, max = 256, message = "State should not be more than 256 characters" )
        String state,
        @NotBlank(message = "City is required")
        @Size(min = 2, max = 256, message = "City should not be more than 256 characters" )
        String city,
        @NotBlank(message = "Terminal Address is required")
        @Size(min = 5, max = 256, message = "Terminal Address should not be more than 256 characters" )
        String address,
        @NotBlank(message = "Supervisor is needed")
        String supervisorId
) {
}
