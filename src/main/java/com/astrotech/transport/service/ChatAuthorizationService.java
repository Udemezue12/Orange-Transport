package com.astrotech.transport.service;

import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.repositories.ChatConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatAuthorizationService {

    private final ChatConversationRepository conversationRepository;


    @CustomCacheable(
            value =  "chat-participants",
            key = "#conversationId + ':' + #userId",
            ttl = 600,
            timeUnit = TimeUnit.SECONDS
    )
    public boolean isParticipant(
            UUID conversationId,
            UUID userId
    ) {
        return conversationRepository.isParticipant(
                conversationId,
                userId
        );
    }
}
