package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.SeatResponse;
import com.astrotech.transport.dto.response.SimpleSeatResponse;
import com.astrotech.transport.entities.Seat;
import com.astrotech.transport.entities.Vehicle;
import com.astrotech.transport.enums.SeatPosition;
import com.astrotech.transport.enums.SeatStatus;

import java.util.List;

public class SeatMapper {
    public static Seat createSeat(Integer rowNumber, SeatStatus status, String seatNumber, SeatPosition position, Vehicle vehicle) {
        return Seat.builder()
                .rowNumber(rowNumber)
                .status(status)
                .seatNumber(seatNumber)
                .position(position)
                .vehicle(vehicle)
                .build();
    }

    public static SimpleSeatResponse simpleResponse(Seat seat) {
        return new SimpleSeatResponse(
                seat.getId(),
                seat.getPosition(),
                seat.getStatus(),
                seat.getSeatNumber(),
                seat.getRowNumber()
        );

    }
    public static SeatResponse toResponse(Seat seat){
        var vehicleResponse = VehicleMapper.toResponse(seat.getVehicle());
        var seatResponse = simpleResponse(seat);
        return new SeatResponse(
                vehicleResponse,
                seatResponse
        );

    }
    public static List<SeatResponse> toListResponse(List<Seat> seats) {
        if (seats == null) {
            return List.of();
        }
        return seats.stream()
                .map(SeatMapper::toResponse)
                .toList();
    }
}
