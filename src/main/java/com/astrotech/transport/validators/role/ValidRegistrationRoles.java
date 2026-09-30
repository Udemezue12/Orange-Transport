package com.astrotech.transport.validators.role;

import jakarta.validation.*;

import java.lang.annotation.*;

@Target({
        ElementType.FIELD,
        ElementType.PARAMETER,
        ElementType.TYPE_USE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = RegistrationRolesValidator.class)
public @interface ValidRegistrationRoles {

    String message() default "One or more roles cannot receive registration codes";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
