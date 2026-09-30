package com.astrotech.transport.dto.request;

import com.astrotech.transport.validators.email.domain.ValidateEmailDomains;
import jakarta.validation.constraints.*;

public record DriverProfileUpdateRequest(
        @Size(min = 2, max = 255)
        String firstName,

        @Size(min = 2, max = 255)
        String lastName,



        @Email(message = "Invalid email format")
        @ValidateEmailDomains
        String email,


        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        String phoneNumber,



        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "Password must be 8–15 characters and include uppercase, lowercase, number, and special character")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 15 characters")
        String newPassword,

        String oldPassword,

        @Size(min = 10, max = 255, message = "Allowed Length Exceeded")
        String licenseNumber,

        String assetId,

        String publicId


) {
}
