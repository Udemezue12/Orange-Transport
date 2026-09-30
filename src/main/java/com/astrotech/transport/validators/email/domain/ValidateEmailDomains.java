package com.astrotech.transport.validators.email.domain;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;
@Documented
@Constraint(validatedBy = EmailDomainValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidateEmailDomains {
    String message() default "Email provider not allowed";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
