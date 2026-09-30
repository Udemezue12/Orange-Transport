package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.*;

import java.util.List;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        String registrationNumber,
        Integer capacity,
        VehicleType vehicleType,
        VehicleBrand brand,
        VehicleStatus status,
        FuelType fuelType,
        TransmissionType transmission,
        Integer manufactureYear,
        VehicleColor color,
        String chassisNumber,
        String engineNumber,
        String vin,
        VehicleClass vehicleClass,
        String model,
        String thumbNail


) {
}
