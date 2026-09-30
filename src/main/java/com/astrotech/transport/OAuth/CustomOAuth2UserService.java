package com.astrotech.transport.OAuth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final OAuthAccountService accountService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oauthUser = super.loadUser(request);

        var provider =
                OAuthProvider.fromRegistrationId(
                        request.getClientRegistration().getRegistrationId());

        var userInfo =
                OAuthUserInfoFactory.fromOAuth2(provider, oauthUser);

        var user = accountService.process(userInfo);

        return new CustomOAuth2User(oauthUser, user);
    }
}


