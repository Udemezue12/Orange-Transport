package com.astrotech.transport.validators.role;


import com.astrotech.transport.enums.UserRole;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleRequired {
    UserRole[] value();
}