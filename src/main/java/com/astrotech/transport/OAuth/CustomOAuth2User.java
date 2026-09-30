package com.astrotech.transport.OAuth;

import com.astrotech.transport.entities.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Getter
public final class CustomOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomOAuth2User(OAuth2User delegate, User user) {
        this.delegate = Objects.requireNonNull(delegate);
        this.user = Objects.requireNonNull(user);

        this.authorities = List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
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
        return user.getEmail();
    }
}
