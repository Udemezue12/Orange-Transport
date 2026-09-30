package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.ChatMessageResponse;
import com.astrotech.transport.entities.*;
import com.astrotech.transport.enums.*;

import java.time.Instant;

public class ChatMessageMapper {
    public static ChatMessage sendMessage(ChatConversation conversation, User sender, MessageStatus status, String contentIv, String cipherText){
        return ChatMessage.builder()
                .createdAt(Instant.now())
                .sentAt(Instant.now())
                .conversation(conversation)
                .status(status)
                .type(ChatMessageType.TEXT)
                .contentIv(contentIv)
                .cipherText(cipherText)
                .sender(sender)
                .build();
                
                
    }
    public static ChatMessageResponse messageResponse(ChatMessage chatMessage, String content, boolean sent){

        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getConversation().getId(),
                chatMessage.getSender().getId(),
                chatMessage.getType(),
                content,
                sent,
                chatMessage.getCreatedAt()
        );
    }
}
