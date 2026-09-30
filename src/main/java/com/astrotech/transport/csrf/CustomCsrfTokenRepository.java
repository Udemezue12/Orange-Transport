package com.astrotech.transport.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

@Component
public class CustomCsrfTokenRepository implements CsrfTokenRepository {

    private final CookieCsrfTokenRepository delegate;

    public CustomCsrfTokenRepository() {
        this.delegate = CookieCsrfTokenRepository.withHttpOnlyFalse();

        this.delegate.setCookieName("XSRF-TOKEN");
        this.delegate.setHeaderName("X-XSRF-TOKEN");
    }

    @Override
    public CsrfToken generateToken(HttpServletRequest request) {
        var token = delegate.generateToken(request);
        System.out.println(
                "Generated CSRF: "
                        + token.getToken());

        return token;
    }

    @Override
    public void saveToken(
            CsrfToken token,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        System.out.println(
                "Saved CSRF: "
                        + (token != null
                        ? token.getToken()
                        : "null"));


        delegate.saveToken(token, request, response);
    }

    @Override
    public CsrfToken loadToken(HttpServletRequest request) {
        var token = delegate.loadToken(request);
        System.out.println(
                "Loaded CSRF: "
                        + (token != null
                        ? token.getToken()
                        : "null"));

        return token;
    }
}


