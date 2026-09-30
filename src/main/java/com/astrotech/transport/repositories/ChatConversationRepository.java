package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public interface ChatConversationRepository
        extends JpaRepository<ChatConversation, UUID> {

    Optional<ChatConversation> findByIdAndPassengerId(
            UUID conversationId,
            UUID passengerId
    );

    Optional<ChatConversation> findByPassengerIdAndStatus(
            UUID passengerId,
            ConversationStatus status
    );

    Slice<ChatConversation> findByStatus(
            ConversationStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(c) > 0
            FROM ChatConversation c
            WHERE c.id = :conversationId
              AND (
                    c.passenger.id = :userId
                    OR c.agent.id = :userId
                  )
            """)
    boolean isParticipant(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );

    Slice<ChatConversation> findAllByPassengerId(UUID userId, Pageable pageable);

    Slice<ChatConversation> findAllBy(Pageable pageable);
}
