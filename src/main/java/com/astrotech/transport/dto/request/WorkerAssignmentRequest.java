package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.AssignedServiceType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WorkerAssignmentRequest(
        @NotNull(message = "Worker Id cannot eb null")
        UUID workerId,

        @NotNull(message = " Service Type is required")
        AssignedServiceType serviceType

) {
}
