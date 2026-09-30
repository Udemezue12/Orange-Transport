package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.SeatPosition;

public record SeatLayout(
        Integer rowNumber,
        String seatNumber,
        SeatPosition position
) {
}
