package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.AssignedServiceType;
import jakarta.validation.constraints.NotNull;

public record WorkerAssignmentStatusUpdateRequest(

        @NotNull(message = " Service Type is required")
        AssignedServiceType serviceType

) {
}
