package com.astrotech.transport.core;


import com.astrotech.transport.dto.request.AuthenticatedUser;
import com.astrotech.transport.dto.response.CurrentUser;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.exceptions.UnAuthenticatedUserException;
import com.astrotech.transport.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetCurrentUser {
    private final UserRepository userRepo;

    public User getCurrentUser() {
        var userId = getCurrentUserIdAndRole().userId();
        return userRepo.findByIdAndStatus(userId, UserStatus.ACTIVE).orElseThrow(
                () -> new ResourceNotFoundException("Invalid data or account has been suspended/deleted")
        );
    }


    public AuthenticatedUser getAuthenticatedUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnAuthenticatedUserException("User not authenticated");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedUser user) {
            return user;
        }

        if ("anonymousUser".equals(principal) || principal == null) {
            throw new UnAuthenticatedUserException("User not authenticated");
        }

        throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
    }


    public Optional<AuthenticatedUser> getAuthenticatedUserOptional() {
        try {
            return Optional.of(getAuthenticatedUser());
        } catch (UnAuthenticatedUserException | IllegalStateException e) {
            return Optional.empty();
        }
    }





    public CurrentUser getCurrentUserIdAndRole() {

        var authenticatedUser = getAuthenticatedUser();

        final UUID userId;
        final UserRole role;
        final boolean emailVerified;

        try {
            userId = UUID.fromString(authenticatedUser.userId());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Principal ID string is not a valid UUID: "
                            + authenticatedUser.userId(),
                    e
            );
        }

        try {
            role = UserRole.valueOf(authenticatedUser.role());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Principal role is not valid: "
                            + authenticatedUser.role(),
                    e
            );
        }
        try {
            emailVerified = getAuthenticatedUser().emailVerified();
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Principal Email Verification check is not valid: "
                            + authenticatedUser.emailVerified(),
                    e
            );
        }

        return new CurrentUser(userId, role, emailVerified);
    }


    public UUID getCurrentUserIdOrNull() {
        return getAuthenticatedUserOptional()
                .map(user -> {
                    try {
                        return UUID.fromString(user.userId());
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .orElse(null);
    }

    public boolean hasAnyRole(UserRole[] roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        var currentRole = getCurrentUserIdAndRole().role();
        if (currentRole == null) {
            return false;
        }
        return Arrays.asList(roles).contains(currentRole);
    }

}
