package com.astrotech.transport.dto.request;

import com.astrotech.transport.enums.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record PaymentRequest(

        @NotNull(
                message = "Payment method is required"
        )
        PaymentMethod paymentMethod

) {
}
