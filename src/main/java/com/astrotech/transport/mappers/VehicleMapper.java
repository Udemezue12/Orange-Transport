package com.astrotech.transport.mappers;

import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.dto.request.VehicleRequest;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.ImageUploadStatus;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class VehicleMapper {
    public static Vehicle createVehicle(VehicleRequest request, String regNumber) {
        ;
        var trimmedModel = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.model(), false);
        var trimmedChassisNumber = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.chassisNumber(), true);
        var trimmedEngineNumber = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.engineNumber(), true);
        var trimmedVin = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.vin(), true);
        return Vehicle.builder()
                .engineNumber(trimmedEngineNumber)
                .uploadStatus(ImageUploadStatus.PENDING)
                .manufactureYear(request.manufactureYear())
                .color(request.vehicleColor())
                .brand(request.brand())
                .status(request.vehicleStatus())
                .model(trimmedModel)
                .registrationNumber(regNumber)
                .vehicleClass(request.vehicleClass())
                .fuelType(request.fuelType())
                .capacity(request.capacity())
                .chassisNumber(trimmedChassisNumber)
                .transmission(request.transmissionType())
                .vin(trimmedVin)
                .vehicleType(request.vehicleType())
                .build();
    }

    public static VehicleResponse toResponse(Vehicle vehicle) {
        var imageUrl = vehicle.getThumbNailUrl() != null ? vehicle.getThumbNailUrl() : null;

        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getRegistrationNumber(),
                vehicle.getCapacity(),
                vehicle.getVehicleType(),
                vehicle.getBrand(),
                vehicle.getStatus(),
                vehicle.getFuelType(),
                vehicle.getTransmission(),
                vehicle.getManufactureYear(),
                vehicle.getColor(),
                vehicle.getChassisNumber(),
                vehicle.getEngineNumber(),
                vehicle.getVin(),
                vehicle.getVehicleClass(),
                vehicle.getModel(),
                imageUrl
        );
    }

    public static VehicleWithImagesAndSeatsResponse toVehicleResponse(
            Vehicle vehicle,
            List<VehicleImages> images
    ) {
        var vehicleResponse = toResponse(vehicle);
        var seatResponse = getSeatList(vehicle);

        return new VehicleWithImagesAndSeatsResponse(
                vehicleResponse,
               seatResponse,

                images.stream()
                        .map(VehicleImageMapper::toUploadResponse)
                        .toList()
        );
    }
    public static VehicleWithSeatsResponse toVehicleWithSeatResponse(
            Vehicle vehicle
    ) {
        var vehicleResponse = toResponse(vehicle);
        var seatResponse = getSeatList(vehicle);

        return new VehicleWithSeatsResponse(
                vehicleResponse,
                seatResponse


        );
    }

    private static @NonNull List<SimpleSeatResponse> getSeatList(Vehicle vehicle) {
        return vehicle.getSeats()
                .stream()
                .map(SeatMapper::simpleResponse)
                .toList();
    }


}
