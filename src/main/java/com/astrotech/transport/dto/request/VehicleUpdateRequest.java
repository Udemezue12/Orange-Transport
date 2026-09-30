package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.*;
import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record VehicleUpdateRequest(



        @Size(max = 255)
        String model,


        @Positive(message = "Capacity must be greater than 0")
        Integer capacity,


        List<@Positive(message = "Seat count per row must be at least 1") Integer> seatsPerRow,


        VehicleBrand brand,

        VehicleType vehicleType,

        FuelType fuelType,


        TransmissionType transmissionType,

        Integer manufactureYear,


        VehicleColor vehicleColor,

        VehicleStatus vehicleStatus,

        VehicleClass vehicleClass,

        String chassisNumber,

        String engineNumber,

        String vin,
        UUID vehicleImageId,
        String assetId,
        String publicId

) {
}
