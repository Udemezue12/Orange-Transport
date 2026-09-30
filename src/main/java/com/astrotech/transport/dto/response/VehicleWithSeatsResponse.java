package com.astrotech.transport.dto.response;

import java.util.List;

public record VehicleWithSeatsResponse(
        VehicleResponse vehicle,
        List<SimpleSeatResponse> seats
        
) {
}
