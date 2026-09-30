package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.TripStatus;
import jakarta.validation.constraints.*;

public record UpdateTripDelayWithReason(

        @Size(min = 10, max = 256, message = "Reason should not exceed 256 characters")
        String delayReason,
        @NotNull(message = "Trip status is required")
        TripStatus status
){

}
