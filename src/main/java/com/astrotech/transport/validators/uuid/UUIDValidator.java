package com.astrotech.transport.validators.uuid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public class UUIDValidator implements ConstraintValidator<ValidUUID, String> {

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context
    ) {
        if (value == null || value.isBlank()) {
            return true;
        }

        try {
            UUID uuid = UUID.fromString(value);

            return uuid.toString().equalsIgnoreCase(value);

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
