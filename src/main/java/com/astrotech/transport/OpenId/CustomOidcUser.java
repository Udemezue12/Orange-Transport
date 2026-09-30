package com.astrotech.transport.OpenId;


import com.astrotech.transport.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public final class CustomOidcUser implements OidcUser {

    private final OidcUser delegate;
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOidcUser(
            OidcUser delegate,
            User user
    ) {
        this.delegate = Objects.requireNonNull(delegate);
        this.user = Objects.requireNonNull(user);

        this.authorities = List.of(
                new SimpleGrantedAuthority(
                        "ROLE_" + user.getRole().name()
                )
        );
    }

    @Override
    public Map<String, Object> getClaims() {
        return delegate.getClaims();
    }

    @Override
    public OidcIdToken getIdToken() {
        return delegate.getIdToken();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return delegate.getUserInfo();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return user.getId().toString();
    }
}
