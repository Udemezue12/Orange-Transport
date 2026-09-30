package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.validators.role.ValidRegistrationRoles;
import jakarta.validation.constraints.*;

public record GenerateRegisterCodeRequest(
        @NotNull(message = "Role is required")
        UserRole role
) {
}

