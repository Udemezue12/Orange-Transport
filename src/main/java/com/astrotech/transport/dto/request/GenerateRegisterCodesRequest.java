package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.UserRole;
import jakarta.validation.constraints.*;

import java.util.List;

public record GenerateRegisterCodesRequest(
        @NotEmpty(message = "At least one role is required")
        @Size(max = 10, message = "You cannot specify more than 10 roles")

        List<UserRole> roles,


        Integer numOfCodes
) {
}
