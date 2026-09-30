package com.astrotech.transport.repositories;

import com.astrotech.transport.OAuth.OAuthProvider;
import com.astrotech.transport.entities.OAuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface OAuthAccountRepository
        extends JpaRepository<OAuthAccount, UUID> {

    Optional<OAuthAccount> findByProviderAndProviderSubject(
            OAuthProvider provider,
            String providerSubject
    );

    boolean existsByProviderAndProviderSubject(
            OAuthProvider provider,
            String providerSubject
    );

    boolean existsByUserIdAndProvider(
            UUID userId,
            OAuthProvider provider
    );
}
