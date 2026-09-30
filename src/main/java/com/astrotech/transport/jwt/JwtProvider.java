package com.astrotech.transport.jwt;


import com.astrotech.transport.config.JwtConfig;
import com.astrotech.transport.dto.response.TokenData;
import com.astrotech.transport.enums.JwtType;
import com.astrotech.transport.enums.UserRole;


import com.astrotech.transport.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;



import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {
    private final JwtConfig jwtConfig;
    private final Jwt jwt;

    public String generateAccessToken(String userId, UserRole role, boolean emailVerified) {
        Map<String, Object> claims = new HashMap<>();
        var expiry = jwtConfig.getAccessExpiration() * 1000;
        var now = System.currentTimeMillis();
        claims.put("type", JwtType.ACCESS.toString());
        claims.put("emailVerified", emailVerified);
        claims.put("role", role.toString());
        return  jwt.buildToken(claims, userId,  jwtConfig.getSecretKey(), new Date(now + expiry), true);

    }
    public String generateRefreshToken(String userId, UserRole role, boolean emailVerified) {
        Map<String, Object> claims = new HashMap<>();
        var expiry = jwtConfig.getRefreshExpiration() * 1000;
        var now = System.currentTimeMillis();
        claims.put("type", JwtType.REFRESH.toString());
        claims.put("emailVerified", emailVerified);

        claims.put("role", role.toString());

        return  jwt.buildToken(claims, userId,  jwtConfig.getSecretKey(),  new Date(now + expiry), true);

    }



    public TokenData extractClaims(String token, JwtType expectedType) {
        return jwt.validateAndExtractClaims(token, true)
                .map(claims -> {
                    JwtType actualType;
                    try {
                        actualType = JwtType.valueOf(
                                claims.get("type", String.class)
                        );
                    } catch (Exception ex) {
                        throw new BadRequestException("Invalid token type.");
                    }

                    if (actualType != expectedType) {
                        throw new BadRequestException(
                                "Expected %s token but received %s token."
                                        .formatted(expectedType, actualType)
                        );
                    }
                    var emailVerified = Boolean.TRUE.equals(
                            claims.get("emailVerified", Boolean.class));

                    return new TokenData(
                            claims.getId(),
                            claims.getSubject(),
                            claims.getExpiration(),
                            true,
                            UserRole.valueOf(claims.get("role", String.class)),
                            actualType,
                            claims.getExpiration().before(new Date()),
                            emailVerified
                    );
                })
                .orElseThrow(() -> new BadRequestException("Invalid or expired token"));
    }









}
