package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.validators.email.domain.ValidateEmailDomains;
import jakarta.validation.constraints.*;



public record RegisterUserRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 255)
        String firstName,
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 255)
        String lastName,
        @NotNull(message = "Role is required")
        UserRole role,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @ValidateEmailDomains
        String email,
        @NotBlank(message = "Phone number is required") @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format") String phoneNumber,

        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "Password must be 8–15 characters and include uppercase, lowercase, number, and special character")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 15 characters")
        String password)
{

}
