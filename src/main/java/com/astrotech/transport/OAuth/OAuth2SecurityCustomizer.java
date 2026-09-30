package com.astrotech.transport.OAuth;

import com.astrotech.transport.configProperties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;

@Configuration
@RequiredArgsConstructor
public class OAuth2SecurityCustomizer {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOidcUserService customOidcUserService;
    private final OAuth2AuthenticationSuccessHandler
            oauth2AuthenticationSuccessHandler;

    private final OAuth2AuthenticationFailureHandler
            oauth2AuthenticationFailureHandler;
    private final AppProperties appProperties;

    public Customizer<OAuth2LoginConfigurer<HttpSecurity>> getOAuth2LoginCustomizer() {
        if (Boolean.TRUE.equals(appProperties.enableOAuth2())) {
            return oauth2 -> oauth2
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService)
                            .oidcUserService(customOidcUserService)
                    )
                    .successHandler( oauth2AuthenticationSuccessHandler)
                    .failureHandler(oauth2AuthenticationFailureHandler);
        }
        return AbstractHttpConfigurer::disable;
    }
}
