package com.astrotech.transport.jwt;


import com.astrotech.transport.config.JwtConfig;
import com.astrotech.transport.configProperties.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Component
@RequiredArgsConstructor
public class JwtResponseCookie {

    private final JwtConfig jwtConfig;
    private final AppProperties appProperties;

    public void setCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken) {
        boolean isSecure = Boolean.TRUE.equals(appProperties.httpSecure());
        boolean enableHttpOnly = Boolean.TRUE.equals(appProperties.enableHttpOnly());
        if (Boolean.TRUE.equals(appProperties.enableCsrf())) {

            ResponseCookie accessCookie = ResponseCookie.from(
                            "access_token",
                            accessToken)
                    .httpOnly(enableHttpOnly)
                    .secure(isSecure)
                    .sameSite("Strict")
                    .path("/")
                    .maxAge(jwtConfig.getAccessExpiration())
                    .build();
            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    accessCookie.toString());
        }

        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refresh_token",
                        refreshToken)
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtConfig.getRefreshExpiration())
                .build();


        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString());
    }

    public void clearCookies(
            HttpServletResponse response) {
        boolean isSecure = Boolean.TRUE.equals(appProperties.httpSecure());
        boolean enableHttpOnly = Boolean.TRUE.equals(appProperties.enableHttpOnly());

        if (Boolean.TRUE.equals(appProperties.enableCsrf())) {
            var accessCookie = ResponseCookie.from("access_token", "")
                    .httpOnly(enableHttpOnly)
                    .path("/")
                    .maxAge(0)
                    .build();
            var csrfCookie = ResponseCookie.from("XSRF-TOKEN", "")
                    .path("/")
                    .httpOnly(false)
                    .maxAge(0)
                    .secure(isSecure)
                    .sameSite("Lax")
                    .build();

            response.addHeader("Set-Cookie", csrfCookie.toString());

            response.addHeader(
                    HttpHeaders.SET_COOKIE,
                    accessCookie.toString());

        }


        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refresh_token",
                        ""
                )
                .httpOnly(true)
                .secure(isSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );
    }

    public String extractJakartaCookie(
            HttpServletRequest request,
            String name) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {

            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String extractSpringCookie(HttpServletRequest request, String name) {
        var cookie = WebUtils.getCookie(request, name);
        return (cookie != null) ? cookie.getValue() : null;
    }
}
