package com.astrotech.transport.csrf;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;


@Component
public class CustomSpaCsrfTokenRequestHandler
        implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestAttributeHandler delegate =
            new CsrfTokenRequestAttributeHandler();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            Supplier<CsrfToken> csrfToken
    ) {
        delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(
            HttpServletRequest request,
            CsrfToken csrfToken
    ) {
        String headerValue =
                request.getHeader(csrfToken.getHeaderName());

        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }

        return csrfToken.getToken();
    }
}
