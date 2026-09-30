package com.astrotech.transport.validators.seats;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SeatIdsValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSeatIds {
    String message() default "One or more invalid seat IDs provided.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
