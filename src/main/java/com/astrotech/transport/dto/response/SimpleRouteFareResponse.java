package com.astrotech.transport.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SimpleRouteFareResponse(
        UUID id,
        BigDecimal amount,
        Instant effectiveFrom,
        Instant effectiveTo
){

}
