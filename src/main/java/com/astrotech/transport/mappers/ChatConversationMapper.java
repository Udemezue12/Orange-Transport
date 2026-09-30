package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.ChatConversation;
import com.astrotech.transport.entities.User;
import com.astrotech.transport.enums.ConversationStatus;

import java.time.Instant;

public class ChatConversationMapper {
    public static ChatConversation createConvo(User passenger) {
        return ChatConversation.builder()
                .passenger(passenger)
                .status(ConversationStatus.OPEN)
                .createdAt(Instant.now())
                .build();

    }

    public static SimpleChatConversationResponse simpleChatConversation(ChatConversation chatConversation) {
        return new SimpleChatConversationResponse(
                chatConversation.getId(),
                chatConversation.getStatus(),
                chatConversation.getCreatedAt(),
                chatConversation.getUpdatedAt()
        );
    }

    public static AssignAgentConversationResponse assignAgentConversationResponse(ChatConversation chatConversation) {
        var simpleConvo = simpleChatConversation(chatConversation);
        var agentName = chatConversation.getAgent().getFullName() != null
                ? chatConversation.getAgent().getFullName()
                : null;
        return new AssignAgentConversationResponse(
                simpleConvo,
                chatConversation.getPassenger().getFullName(),
                agentName
                );
    }

}

