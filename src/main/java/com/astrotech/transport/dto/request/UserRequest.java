package com.astrotech.transport.dto.request;




import com.astrotech.transport.validators.email.domain.ValidateEmailDomains;
import jakarta.validation.constraints.*;


public record UserRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 255)
        String firstName,
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 255)
        String lastName,


        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(min = 6, max = 256)
        @ValidateEmailDomains
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
        String phoneNumber,


        @Size(min = 10, max = 255)
        String inviteCode,


        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,15}$", message = "Password must be 8–15 characters and include uppercase, lowercase, number, and special character")
        @Size(min = 8, max = 15, message = "Password must be between 8 and 15 characters") String password
) {

}
