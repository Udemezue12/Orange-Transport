package com.astrotech.transport.repositories;

import com.astrotech.transport.entities.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;


@Repository
public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, UUID> {



    Slice<ChatMessage> findByConversationIdOrderByCreatedAtAsc(
            UUID conversationId,
            Pageable pageable
    );
}
