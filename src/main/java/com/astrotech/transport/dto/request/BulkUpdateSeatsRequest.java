package com.astrotech.transport.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;


public record BulkUpdateSeatsRequest(


        @NotNull(message = "Vehicle Capacity cannot be empty")
        Integer vehicleCapacity,

        List<Integer> seatsPerRow
) {}