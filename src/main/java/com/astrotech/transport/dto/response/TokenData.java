package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.JwtType;
import com.astrotech.transport.enums.UserRole;

import java.util.Date;

public record TokenData(String id,
                        String userId,
                        Date expiration,
                        boolean isToken,
                        UserRole role,
                        JwtType jwtType,
                        boolean isExpired,
                        boolean emailVerified
) {}
