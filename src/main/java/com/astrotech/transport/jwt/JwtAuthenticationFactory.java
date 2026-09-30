package com.astrotech.transport.jwt;

import com.astrotech.transport.dto.request.AuthenticatedUser;
import com.astrotech.transport.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtAuthenticationFactory {

    public Authentication create(String id, UserRole role, boolean isVerified, boolean setJwtUserDetails, HttpServletRequest request) {
        var principal = new AuthenticatedUser(id, role.name(), isVerified);
        var authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
        );
        if (setJwtUserDetails) {
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        }
        return authentication;
    }
}

