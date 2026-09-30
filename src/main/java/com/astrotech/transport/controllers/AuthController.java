package com.astrotech.transport.controllers;

import com.astrotech.transport.core.GetCurrentUser;
import com.astrotech.transport.dto.request.*;
import com.astrotech.transport.dto.response.AccessTokenResponse;
import com.astrotech.transport.dto.response.RefreshTokenResponse;
import com.astrotech.transport.dto.response.UserResponse;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.service.AuthService;
import com.astrotech.transport.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "For user registration, login, logout, email verification, forgot password and reset password")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final GetCurrentUser getCurrentUser;

    @PostMapping("/passenger/register")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserResponse>> passenger_register(@Valid @RequestBody UserRequest userRequest) {
        return authService.passenger_register(userRequest);
    }

    @PostMapping("/driver/register")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserResponse>> driver_register(@Valid @RequestBody UserRequest userRequest) {
        return authService.driver_register(userRequest);
    }

    @PostMapping("/supervisor/register")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserResponse>> terminal_supervisor_register(@Valid @RequestBody UserRequest userRequest) {
        return authService.terminal_supervisor_register(userRequest);
    }
    @PostMapping("/customerAgent/register")
    @Ratelimit
    public ResponseEntity<ApiResponse<UserResponse>> customer_service_register(@Valid @RequestBody UserRequest userRequest) {
        return authService.customer_service_register(userRequest);
    }


    @PostMapping("/login")
    @Ratelimit
    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(HttpServletResponse response, @Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(response, request, servletRequest);

    }

    @PostMapping("/logout")
    @Ratelimit
    public ResponseEntity<?> logout(HttpServletResponse response, HttpServletRequest request) {
        return authService.logout(request, response);

    }

    @PostMapping("/refresh")
    @Ratelimit
    public RefreshTokenResponse refresh(@CookieValue(value = "refresh_token") String refreshToken, HttpServletResponse response) {
        return authService.refresh(refreshToken, response);
    }

    @PostMapping("/verify-email")
    @Ratelimit(times = 4, seconds = 8)
    public Map<String, String> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {

        return authService.verifyEmail(request);
    }

    @PostMapping("/forgot-password")
    @Ratelimit(times = 4, seconds = 8)
    public Map<String, Object> forgotPassword(@Valid @RequestBody ResendVerificationRequest request) {

        return authService.forgotPassword(request.email());
    }


    @PostMapping("/resend-email-verification-link")
    @Ratelimit(times = 4, seconds = 8)
    public Object resendVerification(@Valid @RequestBody ResendVerificationRequest request) {

        var result = authService.resendVerificationEmail(request.email());

        if (result != null && "Email already verified.".equals(((Map<?, ?>) result).get("message"))) {
            return ResponseEntity.badRequest().body(result);
        }

        return result;
    }

    @PostMapping("/resend-password-verification-link")
    @Ratelimit(times = 4, seconds = 8)
    public Map<String, Object> resendPasswordVerification(@Valid @RequestBody ResendVerificationRequest request) {

        return authService.resendPasswordResetLink(request.email());
    }

    @PostMapping("/reset-password")
    @Ratelimit
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        return authService.resetPassword(request);

    }

    @PostMapping("/account-delete")
    @Ratelimit
    public Map<String, String> deleteAccount() {
        var userId = getCurrentUser.getCurrentUserIdAndRole().userId();
        return userService.deleteAccount(userId);
    }


}
