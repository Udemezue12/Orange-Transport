package com.astrotech.transport.validators.role;

import com.astrotech.transport.core.GetCurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ValidateRole {
    private final GetCurrentUser getCurrentUser;

    @Before("@annotation(roleRequired)")
    public void verifyRole(RoleRequired roleRequired) {

        if (!getCurrentUser.hasAnyRole(roleRequired.value())) {

            log.warn("User {} failed authorization. Required roles: {}",
                    getCurrentUser.getCurrentUserIdAndRole().userId(), Arrays.toString(roleRequired.value()));


            throw new AccessDeniedException("Access denied. You do not have permission to access this resource.");
        }
    }
}
