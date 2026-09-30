package com.astrotech.transport.security;

import com.astrotech.transport.dto.request.AuthenticatedUser;
import com.astrotech.transport.enums.UserRole;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthenticationFactory {
    private final AuthenticationManager authenticationManager;

    public AuthenticationFactory(@Lazy AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public Authentication login(String email, String password) {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );


        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;


    }

    public Authentication create(String Id, UserRole role, boolean isVerified, boolean setJwtUerDetails, HttpServletRequest request) {


        var principal = new AuthenticatedUser(
                Id, role.name(), isVerified
        );

        var authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + role.name()
                        )
                )
        );
        if (setJwtUerDetails) {
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
        }
        return authentication;
    }
}
