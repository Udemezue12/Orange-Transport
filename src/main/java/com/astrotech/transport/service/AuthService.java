package com.astrotech.transport.service;


import com.astrotech.transport.configProperties.AppProperties;
import com.astrotech.transport.core.ApiCacheControl;
import com.astrotech.transport.core.OTPRatelimit;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.dto.request.LoginRequest;
import com.astrotech.transport.dto.request.ResetPasswordRequest;
import com.astrotech.transport.dto.request.UserRequest;
import com.astrotech.transport.dto.request.VerifyEmailRequest;
import com.astrotech.transport.dto.response.AccessTokenResponse;
import com.astrotech.transport.dto.response.RefreshTokenResponse;
import com.astrotech.transport.dto.response.UserResponse;
import com.astrotech.transport.enums.CodeStatus;
import com.astrotech.transport.enums.JwtType;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.jobrunr.tasks.SendVerifyAndPasswordResetEmail;
import com.astrotech.transport.jwt.JwtTokenIssuanceAndRemoval;
import com.astrotech.transport.mappers.ProfileMapper;
import com.astrotech.transport.mappers.TokenMapper;
import com.astrotech.transport.mappers.UserMapper;
import com.astrotech.transport.repositories.ProfileRepository;
import com.astrotech.transport.repositories.UserRepository;
import com.astrotech.transport.responseBuilder.ApiResponse;
import com.astrotech.transport.responseBuilder.ApiResponseBuilder;
import com.astrotech.transport.security.AuthenticationFactory;
import com.astrotech.transport.verification.UserVerification;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;

    private final JwtTokenIssuanceAndRemoval tokenIssuanceAndRemoval;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final UserVerification verificationService;
    private final JobScheduler jobScheduler;
    private final SendVerifyAndPasswordResetEmail sendVerifyAndPasswordResetEmail;
    private final AuthenticationFactory authenticationFactory;
    private final OTPRatelimit otpRatelimit;

    private final ApiCacheControl apiCacheControl;
    private final GenerateRegisterCodeService registrationCodeService;



    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> passenger_register(UserRequest request) {


        var trimmedResult = TrimWhiteSpace.getTrimmedResult(request.email(), request.firstName(), request.lastName());
        validateUserFields(trimmedResult.trimmedEmail(), trimmedResult.fullName(), request.phoneNumber());


        return createUser(request.password(), trimmedResult.trimmedEmail(), request.phoneNumber(), UserRole.PASSENGER, trimmedResult.fullName());
    }

    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> driver_register(UserRequest request) {


        var trimmedResult = TrimWhiteSpace.getTrimmedResult(request.email(), request.firstName(), request.lastName());
        var role = UserRole.DRIVER;
        if (request.inviteCode() == null || request.inviteCode().isBlank()) {
            throw new BadRequestException("Registration code is required for role: " + role);
        }
        validateUserFields(trimmedResult.trimmedEmail(), trimmedResult.fullName(), request.phoneNumber());


        registrationCodeService.checkAndUpdateCodeStatus(request.inviteCode(), role, CodeStatus.VALID, CodeStatus.INVALID);

        return createUser(request.password(), trimmedResult.trimmedEmail(), request.phoneNumber(), role, trimmedResult.fullName());
    }

    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> terminal_supervisor_register(UserRequest request) {

        var trimmedResult = TrimWhiteSpace.getTrimmedResult(request.email(), request.firstName(), request.lastName());
        var role = UserRole.TERMINAL_SUPERVISOR;
        if (request.inviteCode() == null || request.inviteCode().isBlank()) {
            throw new BadRequestException("Registration code is required for role: " + role);
        }
        validateUserFields(trimmedResult.trimmedEmail(), trimmedResult.fullName(), request.phoneNumber());


        registrationCodeService.checkAndUpdateCodeStatus(request.inviteCode(), role, CodeStatus.VALID, CodeStatus.INVALID);

        return createUser(request.password(), trimmedResult.trimmedEmail(), request.phoneNumber(), role, trimmedResult.fullName());

    }
    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> vehicle_loader_register(UserRequest request) {

        var trimmedResult = TrimWhiteSpace.getTrimmedResult(request.email(), request.firstName(), request.lastName());
        var role = UserRole.VEHICLE_LOADER;
        if (request.inviteCode() == null || request.inviteCode().isBlank()) {
            throw new BadRequestException("Registration code is required for role: " + role);
        }
        validateUserFields(trimmedResult.trimmedEmail(), trimmedResult.fullName(), request.phoneNumber());


        registrationCodeService.checkAndUpdateCodeStatus(request.inviteCode(), role, CodeStatus.VALID, CodeStatus.INVALID);

        return createUser(request.password(), trimmedResult.trimmedEmail(), request.phoneNumber(), role, trimmedResult.fullName());

    }
    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> customer_service_register(UserRequest request) {

        var trimmedResult = TrimWhiteSpace.getTrimmedResult(request.email(), request.firstName(), request.lastName());
        var role = UserRole.CUSTOMER_SERVICE_AGENT;
        if (request.inviteCode() == null || request.inviteCode().isBlank()) {
            throw new BadRequestException("Registration code is required for role: " + role);
        }
        validateUserFields(trimmedResult.trimmedEmail(), trimmedResult.fullName(), request.phoneNumber());

        registrationCodeService.verifyCodeAndRoleOrThrow(request.inviteCode(), role);

        return createUser(request.password(), trimmedResult.trimmedEmail(), request.phoneNumber(), role, trimmedResult.fullName());

    }

    @Transactional
    public ResponseEntity<ApiResponse<UserResponse>> admin_register(UserRequest request) {

        var trimmedResult = TrimWhiteSpace.getTrimmedResult(request.email(), request.firstName(), request.lastName());
        validateUserFields(trimmedResult.trimmedEmail(), trimmedResult.fullName(), request.phoneNumber());
        var role = UserRole.ADMIN;
        if (Boolean.TRUE.equals(appProperties.requestRegisterCodeForAdmins())) {
                if (request.inviteCode() == null || request.inviteCode().isBlank()) {
            throw new BadRequestException(
                    "Registration code is required for role: " + role
            );
        }
            registrationCodeService.checkAndUpdateCodeStatus(request.inviteCode(), role, CodeStatus.VALID, CodeStatus.INVALID);
        }

        return createUser(request.password(), trimmedResult.trimmedEmail(), request.phoneNumber(), role, trimmedResult.fullName());

    }


    @Transactional
    public ResponseEntity<ApiResponse<AccessTokenResponse>> login(HttpServletResponse response, LoginRequest request, HttpServletRequest servletRequest) {
        var email = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(request.email(), false);
        authenticationFactory.login(email, request.password());
        var allowedStatuses = List.of(UserStatus.ACTIVE);

        var user = userRepository.findByEmailAndStatuses(email, allowedStatuses).orElseThrow(() -> new BadRequestException("Invalid credentials"));
        var issuedToken = tokenIssuanceAndRemoval.issueJwtToken(user, response);


        user.setLastLoginAt(Instant.now());
        var tokenMapper = TokenMapper.accessTokenResponse(issuedToken.accessToken(), issuedToken.refreshToken(), user);


        return ApiResponseBuilder.success(
                "Logged in Successfully",
                tokenMapper,
                apiCacheControl.noStore()
        );
    }


    public ResponseEntity<?> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        tokenIssuanceAndRemoval.deleteJwtToken(request, response);


        return ResponseEntity.ok(
                Map.of("message", "Logout successful"));
    }


    public RefreshTokenResponse refresh(String refreshToken, HttpServletResponse response) {

        var claims = tokenIssuanceAndRemoval.validateClaims(refreshToken, JwtType.REFRESH);


        var user = userRepository.findById(UUID.fromString(claims.userId()))
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("User account is unavailable");
        }

        var reIssuedTokens = tokenIssuanceAndRemoval.reIssueJwtToken(refreshToken, response, claims.jwtType(), claims.userId(), claims.role(), claims.emailVerified());


        return TokenMapper.refreshTokenResponse(
                reIssuedTokens.accessToken(),
                reIssuedTokens.refreshToken()
        );

    }


    public Map<String, String> resendVerificationEmail(
            String email) {
        var trimmedEmail = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(email, false);
        var allowedStatuses = List.of(UserStatus.ACTIVE);
        var user = userRepository.findByEmailAndStatuses(trimmedEmail, allowedStatuses)
                .orElse(null);

        if (user != null) {

            if (user.isVerified()) {

                return Map.of(
                        "message",
                        "Email already verified.");
            }

            var allowed = otpRatelimit
                    .canRequestOtp(user.getEmail());

            if (allowed) {

                var otp = verificationService
                        .generateOtp(user.getEmail());

                var token = verificationService
                        .generateVerifyToken(
                                user.getEmail());

                otpRatelimit
                        .recordOtp(
                                user.getEmail());
                var userEmail = user.getEmail();
                var name = user.getFullName();
                var phoneNumber = user.getPhoneNumber();

                jobScheduler.enqueue(
                        () -> sendVerifyAndPasswordResetEmail.sendVerificationEmail(userEmail,
                                otp,
                                name,
                                token));
                jobScheduler.enqueue(
                        () -> sendVerifyAndPasswordResetEmail.sendVerificationSms(
                                phoneNumber,
                                otp,
                                name

                        ));
            }
        }

        return Map.of(
                "message",
                "If the email exists, a verification message has been sent");
    }


    public Map<String, Object> resendPasswordResetLink(
            String email) {

        var allowedStatuses = List.of(UserStatus.ACTIVE);
        var user = userRepository.findByEmailAndStatuses(email, allowedStatuses)
                .orElse(null);

        Long retryAfter = null;

        if (user != null) {

            boolean allowed = otpRatelimit
                    .canRequestOtp(user.getEmail());

            if (allowed) {

                var token = verificationService
                        .generateResetToken(
                                user.getEmail());

                var otp = verificationService
                        .generateOtp(
                                user.getEmail());
                var userEmail = user.getEmail();
                var name = user.getFullName();
                var phoneNumber = user.getPhoneNumber();

                otpRatelimit
                        .recordOtp(
                                user.getEmail());

                jobScheduler.enqueue(
                        () -> sendVerifyAndPasswordResetEmail
                                .sendPasswordResetEmail(
                                        userEmail,
                                        otp,
                                        name,
                                        token));
                jobScheduler.enqueue(
                        () -> sendVerifyAndPasswordResetEmail
                                .sendPasswordResetSms(
                                        phoneNumber,
                                        otp,
                                        name

                                ));
            }

            retryAfter = otpRatelimit
                    .getRetryAfter(
                            user.getEmail());
        }

        assert retryAfter != null;
        return Map.of(
                "message",
                "If the email exists, a reset link has been sent",
                "retry_after",
                retryAfter);
    }


    public Map<String, Object> forgotPassword(
            String email) {

        return resendPasswordResetLink(email);
    }

    @Transactional
    public Map<String, String> verifyEmail(VerifyEmailRequest request) {

        boolean tokenEnabled = Boolean.TRUE.equals(
                appProperties.enableSmsToken()
        );

        boolean hasOtp = request.otp() != null;
        boolean hasToken = request.token() != null;

        if (hasOtp == hasToken) {
            throw new BadRequestException(
                    "Provide either an OTP or a token"
            );
        }

        if (hasToken && !tokenEnabled) {
            throw new BadRequestException(
                    "Token verification is currently disabled"
            );
        }

        String email = hasOtp
                ? verificationService.verifyOtp(request.otp())
                : verificationService.verifyVerifyToken(request.token());

        if (email == null) {
            throw new BadRequestException(
                    "Invalid or expired token/link"
            );
        }

        var user = userRepository.findByEmailAndStatuses(
                email,
                List.of(UserStatus.ACTIVE)
        ).orElseThrow(() -> new ResourceNotFoundException(
                "If the email exists, a reset link has been sent"
        ));

        user.setVerified(true);
        user.setVerifiedAt(Instant.now());

        return Map.of(
                "message",
                "Email verified successfully"
        );
    }

    @Transactional

    public Map<String, String> resetPassword(
            ResetPasswordRequest request
    ) {
        boolean tokenEnabled = Boolean.TRUE.equals(
                appProperties.enableSmsToken()
        );

        boolean hasToken = request.token() != null
                && !request.token().isBlank();

        boolean hasOtp = request.otp() != null
                && !request.otp().isBlank();


        if (hasToken == hasOtp) {
            throw new BadRequestException(
                    "Provide either an OTP or a token"
            );
        }

        if (hasToken && !tokenEnabled) {
            throw new BadRequestException(
                    "Token verification is currently disabled"
            );
        }

        String email = hasOtp
                ? verificationService.verifyOtp(request.otp())
                : verificationService.verifyResetToken(request.token());

        if (email == null) {
            throw new BadRequestException(
                    "Invalid or expired token"
            );
        }

        var user = userRepository.findByEmailAndStatuses(
                email,
                List.of(UserStatus.ACTIVE)
        ).orElseThrow(() -> new ResourceNotFoundException(
                "Invalid reset data"
        ));

        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );

        return Map.of(
                "message",
                "Password reset successfully"
        );
    }

    private ResponseEntity<ApiResponse<UserResponse>> createUser(String password, String email, String phoneNumber, UserRole role, String fullName) {
        var encodedPassword = passwordEncoder.encode(password);
        var user = UserMapper.create(phoneNumber, email, fullName, encodedPassword, role);
        var savedUser = userRepository.save(user);

        if (savedUser.getRole() != UserRole.DRIVER) {
            var profile = ProfileMapper.create(savedUser);
            profileRepository.save(profile);
        }

        var userDto = UserMapper.response(savedUser);
        if (userDto.email() != null && !userDto.email().isBlank()) {
            resendVerificationEmail(userDto.email());
        }

        return ApiResponseBuilder.success("Registered successfully! " +
                        "Please click the link in your email or enter the OTP sent to your phone " +
                        "and email to verify your account.",
                null,
                apiCacheControl.noStore());
    }

    public void validateUserFields(String email, String fullName, String phoneNumber) {


        var existingUsers = userRepository.findExistingFields(email, fullName, phoneNumber);

        if (!existingUsers.isEmpty()) {
            if (existingUsers.stream().anyMatch(u -> email.equalsIgnoreCase(u.getEmail()))) {
                throw new BadRequestException("Email already exists");
            }
            if (existingUsers.stream().anyMatch(u -> fullName.equalsIgnoreCase(u.getFullName()))) {
                throw new BadRequestException("Name already exists");
            }
            if (existingUsers.stream().anyMatch(u -> phoneNumber.equals(u.getPhoneNumber()))) {
                throw new BadRequestException("PhoneNumber already exists");
            }
        }
    }


}
