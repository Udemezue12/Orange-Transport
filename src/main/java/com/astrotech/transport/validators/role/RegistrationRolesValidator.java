package com.astrotech.transport.validators.role;

import com.astrotech.transport.enums.UserRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class RegistrationRolesValidator
        implements ConstraintValidator<ValidRegistrationRoles, List<UserRole>> {

    private static final Set<UserRole> ALLOWED_ROLES =
            EnumSet.of(
                    UserRole.ADMIN,
                    UserRole.CUSTOMER_SERVICE_AGENT,
                    UserRole.DRIVER,
                    UserRole.TERMINAL_SUPERVISOR
            );

    @Override
    public boolean isValid(
            List<UserRole> roles,
            ConstraintValidatorContext context
    ) {
        if (roles == null || roles.isEmpty()) {
            return false;
        }

        return ALLOWED_ROLES.containsAll(roles);
    }
}

