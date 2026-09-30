package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record DriverProfileRequest(

        @NotBlank(message = "License Number is required")
        @Size(min = 10, max = 255, message = "Allowed Length Exceeded")
        String licenseNumber,


        String publicId,
        String assetId


) {


}
