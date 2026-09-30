package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.TripStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTripStatus(
        @NotNull(message = "Trip status is required")
        TripStatus status
){
        
}

