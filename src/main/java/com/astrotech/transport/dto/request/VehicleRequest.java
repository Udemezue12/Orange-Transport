package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.*;
import jakarta.validation.constraints.*;

import java.util.List;

public record VehicleRequest(
        List<VehicleImageRequest> images,
        @NotBlank(message = "Vehicle Model Number required")
        String model,


        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be greater than 0")
        Integer capacity,

        @NotEmpty(message = "Seats per row configuration cannot be empty or null")
        List<@NotNull(message = "Seat count per row cannot be null")
        @Positive(message = "Seat count per row must be at least 1") Integer> seatsPerRow,


        @NotNull(message = "Vehicle Brand is required")
        VehicleBrand brand,
        @NotNull(message = "Fuel Type is required")
        VehicleType vehicleType,

        @NotNull(message = "Fuel Type is required")
        FuelType fuelType,

        @NotNull(message = "Transmission Type is required")
        TransmissionType transmissionType,
        @NotNull(message = "Manufacture year is required")
        Integer manufactureYear,

        @NotNull(message = "Vehicle Color is required")
        VehicleColor vehicleColor,
        @NotNull(message = "Vehicle Status is required")
        VehicleStatus vehicleStatus,
        @NotNull(message = "Vehicle Class is required")
        VehicleClass vehicleClass,
        @NotBlank(message = "Chassis Number is required")
        String chassisNumber,
        @NotBlank(message = "Engine Number is required")
        String engineNumber,
        @NotBlank(message = "Vin Number is required")
        String vin


) {


}
