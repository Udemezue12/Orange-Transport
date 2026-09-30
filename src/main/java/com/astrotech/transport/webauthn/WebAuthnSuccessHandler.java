package com.astrotech.transport.webauthn;

import com.astrotech.transport.dto.response.PasskeyLoginResponse;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.UserStatus;
import com.astrotech.transport.jwt.JwtTokenIssuanceAndRemoval;
import com.astrotech.transport.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WebAuthnSuccessHandler
        implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenIssuanceAndRemoval jwtTokenIssuanceAndRemoval;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        String username = authentication.getName();

        User user = userRepository
                .findByEmailAndStatuses(username, List.of(UserStatus.ACTIVE))
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Authenticated user no longer exists"
                        )
                );

        var tokens = jwtTokenIssuanceAndRemoval.issueJwtToken(
                user,
                response
        );

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        new PasskeyLoginResponse(
                                true,
                                tokens.accessToken(),
                                tokens.refreshToken()
                        )
                )
        );
    }
}
