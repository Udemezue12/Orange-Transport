package com.astrotech.transport.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DriverLicenseResponse(
        @JsonProperty("entity") LicenceEntity entity
) {
}
