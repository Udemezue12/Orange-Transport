package com.astrotech.transport.OAuth;



import com.astrotech.transport.core.PasswordGenerator;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.enums.UserStatus;
import com.astrotech.transport.repositories.*;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OAuthAccountService {

    private final OAuthAccountRepository oauthAccountRepository;
    private final UserRepository userRepository;

    private final PasswordGenerator passwordGenerator;
    private final PasswordEncoder passwordEncoder;

    private final Clock clock;

    @Transactional
    public User process(OAuthUserInfo info) {

        validate(info);


        var existingAccount =
                oauthAccountRepository
                        .findByProviderAndProviderSubject(
                                info.provider(),
                                info.subject()
                        );

        if (existingAccount.isPresent()) {
            return handleExistingAccount(
                    existingAccount.get(),
                    info
            );
        }


        var existingUser =
                userRepository.findByEmailAndStatuses(
                        info.email(),
                        List.of(UserStatus.ACTIVE)
                );

        if (existingUser.isPresent()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_exists"),
                    "An account with this email already exists. " +
                            "Sign in with your existing ORANGE login before linking " +
                            "this OAuth provider."
            );
        }


        var user = createUser(info);

        createOAuthAccount(
                user,
                info
        );

        return user;
    }

    private User handleExistingAccount(
            OAuthAccount account,
            OAuthUserInfo info
    ) {

        var user = getUser(account);


        updateSafeProfileFields(
                user,
                info
        );

        if (info.emailVerified() && !user.isVerified()) {
            user.setVerified(true);
            user.setVerifiedAt(
                    Instant.now(clock)
            );
        }

        user.setEmail(info.email());
        user.setVerified(info.emailVerified());
        account.setLastLoginAt(
                Instant.now(clock)
        );

        user.setLastLoginAt(
                Instant.now(clock)
        );

        return userRepository.save(user);
    }

    private static @NonNull User getUser(OAuthAccount account) {
        var user = account.getUser();

        if (user.isDeleted()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_deleted"),
                    "This account has been deleted."
            );
        }

        if (user.isSuspended()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_suspended"),
                    "This account has been suspended."
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_inactive"),
                    "This account is not active."
            );
        }
        return user;
    }

    private User createUser(
            OAuthUserInfo info
    ) {

        Instant now = Instant.now(clock);

        String generatedPassword =
                passwordGenerator.generate();

        String passwordHash =
                passwordEncoder.encode(generatedPassword);

        return userRepository.save(
                User.builder()
                        .email(info.email())
                        .fullName(info.fullName())
                        .password(passwordHash)
                        .role(UserRole.PASSENGER)
                        .status(UserStatus.ACTIVE)
                        .verified(info.emailVerified())
                        .verifiedAt(
                                info.emailVerified()
                                        ? now
                                        : null
                        )
                        .lastLoginAt(now)
                        .deleted(false)
                        .suspended(false)
                        .build()
        );
    }

    private void createOAuthAccount(
            User user,
            OAuthUserInfo info
    ) {

        OAuthAccount account =
                OAuthAccount.builder()
                        .user(user)
                        .provider(info.provider())
                        .providerSubject(info.subject())
                        .createdAt(Instant.now(clock))
                        .lastLoginAt(Instant.now(clock))
                        .build();

        oauthAccountRepository.save(account);
    }

    private void updateSafeProfileFields(
            User user,
            OAuthUserInfo info
    ) {

        if (StringUtils.hasText(info.fullName())
                && !info.fullName().equals(user.getFullName())) {

            user.setFullName(
                    info.fullName().trim()
            );
        }
    }

    private void validate(
            OAuthUserInfo info
    ) {

        if (!StringUtils.hasText(info.subject())) {
            throw authenticationException(
                    "missing_subject",
                    "OAuth provider did not return a subject."
            );
        }

        if (!StringUtils.hasText(info.email())) {
            throw authenticationException(
                    "missing_email",
                    "OAuth provider did not return an email."
            );
        }

        if (info.provider() == null) {
            throw authenticationException(
                    "missing_provider",
                    "OAuth provider is missing."
            );
        }
    }

    private OAuth2AuthenticationException authenticationException(
            String errorCode,
            String message
    ) {

        return new OAuth2AuthenticationException(
                new OAuth2Error(errorCode),
                message
        );
    }
}
