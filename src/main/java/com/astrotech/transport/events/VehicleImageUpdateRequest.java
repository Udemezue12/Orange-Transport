package com.astrotech.transport.events;

import java.util.List;
import java.util.UUID;

public record VehicleImageUpdateRequest(
        UUID vehicleId,
        List<VehicleImageReplacement> replacements
) {}
