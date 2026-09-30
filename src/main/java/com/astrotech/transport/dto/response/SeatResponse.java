package com.astrotech.transport.dto.response;



public record SeatResponse(
        VehicleResponse vehicle,
        SimpleSeatResponse seat

) {
}
