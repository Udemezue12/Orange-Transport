package com.astrotech.transport.service;

import com.astrotech.transport.core.GetPageRequest;
import com.astrotech.transport.customCache.CustomCacheable;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.entities.ChatMessage;
import com.astrotech.transport.enums.ConversationStatus;
import com.astrotech.transport.enums.MessageStatus;
import com.astrotech.transport.enums.UserRole;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.mappers.ChatMessageMapper;
import com.astrotech.transport.repositories.*;
import com.astrotech.transport.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatConversationService chatConversationService;
    private final ChatMessageRepository messageRepository;
    private final UserService userService;
    private final EncryptionUtil encryptionUtil;
    private final ChatAuthorizationService chatAuthorizationService;



    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "conversation_messages", allEntries = true)
    })
    public ChatMessageResponse createMessage(
            UUID conversationId,
            UUID userId,
            SendChatMessageRequest request
    ) {
        var conversation = chatConversationService.findConvoById(conversationId);

        if (conversation.getStatus() == ConversationStatus.CLOSED) {
            throw new BadRequestException("Conversation with id " + conversationId + " has been closed, please create a new one");
        }

        var sender = userService.getAuthorizedUser(userId);

        chatAuthorizationService.isParticipant(conversation.getId(), sender.getId());

        var isPassenger = conversation.getPassenger().getId().equals(sender.getId());
        UUID recipientId = null;

        if (isPassenger) {
            if (conversation.getAgent() != null) {
                recipientId = conversation.getAgent().getId();
            }
        } else {
            recipientId = conversation.getPassenger().getId();
        }

        if (recipientId != null && recipientId.equals(sender.getId())) {
            throw new BadRequestException("You cannot send a message to yourself");
        }

        var encryptedData = encryptionUtil.encrypt(request.content());

        var messageMapper = ChatMessageMapper.sendMessage(
                conversation,
                sender,
                MessageStatus.SENT,
                encryptedData.iv(),
                encryptedData.cipherText()
        );

        var savedMessage = messageRepository.save(messageMapper);
        var content = encryptionUtil.decryptContent(savedMessage.getCipherText(), savedMessage.getContentIv());

        return ChatMessageMapper.messageResponse(savedMessage, content, true);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "messages", key = "#messageId"),
            @CacheEvict(value = "conversation_messages", allEntries = true)
    })
    public void markMessageDelivered(String messageId, UUID currentUserId) {
        var message = messageRepository.findById(UUID.fromString(messageId)).orElse(null);
        if (message != null) {
            if (!currentUserId.equals(message.getSender().getId())) {
                message.setDeliveredAt(Instant.now());
                message.setDelivered(true);
                messageRepository.save(message);
            }
        }
    }


    @Cacheable(value = "messages", key = "#messageId")
    public ChatMessageResponse getMessageById(String messageId) {
        var message = messageRepository.findById(UUID.fromString(messageId))
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        chatAuthorizationService.isParticipant(message.getConversation().getId(), message.getSender().getId());
        var content = encryptionUtil.decryptContent(message.getCipherText(), message.getContentIv());
        return ChatMessageMapper.messageResponse(message, content, true);
    }
    @CustomCacheable(
            value = "conversation-messages",
            key = "'convoMessage' + #conversationId + 'all-p' + #page + '-s' + #size",
            ttl = 180,
            timeUnit = TimeUnit.SECONDS
    )
    public SliceResponse<ChatMessageResponse> getMessages(UUID conversationId, UUID userId, UserRole role, int page, int size) {
        var pageable = GetPageRequest.getPageableWithSorting(page, size, "createdAt", true, ChatMessage.class, true);
        boolean isAllowed = chatAuthorizationService.isParticipant(conversationId, userId)
                || role == UserRole.ADMIN;

        if (!isAllowed) {
            throw new AccessDeniedException("You do not have access to this conversation");
        }


        var result = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable);


        var content = result.getContent()
                .stream()
                .map(c -> {
                    var finalContent = encryptionUtil.decryptContent(
                            c.getCipherText(),
                            c.getContentIv()
                    );

                    return ChatMessageMapper.messageResponse(c, finalContent, true);
                })
                .toList();
        return new SliceResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.hasNext(),
                result.hasPrevious()


        );

    }


}
