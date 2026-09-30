package com.astrotech.transport.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import com.astrotech.transport.entities.BlacklistedToken;
import org.springframework.stereotype.Repository;


@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
    boolean existsByJti(String jti);


     @Modifying
    @Query("DELETE FROM BlacklistedToken")
    void deleteAllTokens();
}
