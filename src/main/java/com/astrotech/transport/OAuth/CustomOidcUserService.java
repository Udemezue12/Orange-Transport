package com.astrotech.transport.OAuth;

import com.astrotech.transport.OpenId.CustomOidcUser;
import com.astrotech.transport.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService
        extends OidcUserService {

    private final OAuthAccountService accountService;

    @Override
    public OidcUser loadUser(
            OidcUserRequest request
    ) throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(request);

        OAuthProvider provider =
                OAuthProvider.fromRegistrationId(
                        request.getClientRegistration()
                                .getRegistrationId()
                );

        OAuthUserInfo userInfo =
                OAuthUserInfoFactory.fromOidc(
                        provider,
                        oidcUser
                );

        User user = accountService.process(userInfo);

        return new CustomOidcUser(
                oidcUser,
                user
        );
    }
}
