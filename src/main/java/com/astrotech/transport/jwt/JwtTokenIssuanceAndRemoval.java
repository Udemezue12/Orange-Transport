package com.astrotech.transport.jwt;

import com.astrotech.transport.dto.response.TokenData;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.JwtType;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.service.BlacklistedTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenIssuanceAndRemoval {
    private final JwtProvider jwtProvider;
    private final JwtResponseCookie responseCookie;
    private final Jwt jwtService;
    private final BlacklistedTokenService blacklistedTokenService;


    public TokenData validateClaims(String refreshToken, JwtType jwtType) {
        var claims = jwtProvider.extractClaims(refreshToken, jwtType);
        if (blacklistedTokenService.isBlacklisted(claims.id())) {
            throw new BadRequestException("Refresh token already revoked");
        }
        return claims;
    }


    public JwtTokenResult issueJwtToken(User user, HttpServletResponse response) {
        var accessToken = jwtProvider.generateAccessToken(user.getId().toString(), user.getRole(), user.isVerified());

        var refreshToken = jwtProvider.generateRefreshToken(user.getId().toString(), user.getRole(), user.isVerified());
        responseCookie.setCookies(response, accessToken, refreshToken);
        return new JwtTokenResult(
                accessToken,
                refreshToken
        );
    }

    public void deleteJwtToken(HttpServletRequest request,
                               HttpServletResponse response) {
        var accessToken = responseCookie.extractJakartaCookie(request, "access_token");
        var refreshToken = responseCookie.extractJakartaCookie(request, "refresh_token");


        var validAccessToken = jwtService.validateAndExtractClaims(accessToken, true);
        var validRefreshToken = jwtService.validateAndExtractClaims(refreshToken, true);


        validAccessToken.ifPresentOrElse(
                token -> blacklistToken(accessToken, JwtType.ACCESS),
                () -> log.warn("Access token was missing or invalid during logout, skipping blacklist.")
        );

        validRefreshToken.ifPresentOrElse(
                token -> blacklistToken(refreshToken, JwtType.REFRESH),
                () -> log.warn("Refresh token was missing or invalid during logout, skipping blacklist.")
        );


        responseCookie.clearCookies(response);
        SecurityContextHolder.clearContext();
    }

    public JwtTokenResult reIssueJwtToken(String refreshToken, HttpServletResponse response, JwtType jwtType, String userId, UserRole role, boolean emailVerified) {
        var jwt = jwtProvider.extractClaims(refreshToken, jwtType);
        var jti = jwt.id();
        var expiration = jwt.expiration();

        var blacklisted = blacklistedTokenService.blacklist(refreshToken, jwtType, jti, userId, expiration);
        if (blacklisted == null) {
            throw new BadRequestException("Failed to blacklist token");
        }


        var newAccessToken = jwtProvider.generateAccessToken(userId, role, emailVerified);
        var newRefreshToken = jwtProvider.generateRefreshToken(userId, role, emailVerified);

        responseCookie.setCookies(
                response,
                newAccessToken,
                newRefreshToken);
        return new JwtTokenResult(
                newAccessToken,
                newRefreshToken
        );
    }

    private void blacklistToken(String token, JwtType type) {
        var jwt = jwtProvider.extractClaims(token, type);
        blacklistedTokenService.blacklist(
                token,
                type,
                jwt.id(),
                jwt.userId(),
                jwt.expiration()
        );
    }
}
