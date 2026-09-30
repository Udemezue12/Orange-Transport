package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.SeatPosition;
import com.astrotech.transport.enums.SeatStatus;

import java.util.UUID;

public record SimpleSeatResponse(
        UUID id,
        SeatPosition position,
        SeatStatus status,
        String seatNumber,
        Integer rowNumber
) {
}

