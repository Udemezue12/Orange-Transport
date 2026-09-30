package com.astrotech.transport.websocket;

import com.astrotech.transport.dto.request.AuthenticatedUser;
import com.astrotech.transport.service.ChatAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketConversationAuthorizationInterceptor
        implements ChannelInterceptor {

    private final ChatAuthorizationService chatAuthorizationService;

    @Override
    public Message<?> preSend(
            @NonNull Message<?> message,
            @NonNull MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        if (!StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }

        String destination = accessor.getDestination();

        if (destination == null) {
            throw new AccessDeniedException("Missing subscription destination");
        }

        if (!destination.startsWith("/topic/conversation/")) {
            return message;
        }

        UUID conversationId = extractConversationId(destination);

        var principal = accessor.getUser();

        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated subscription");
        }

        AuthenticatedUser user =
                WebSocketUtils.extractUserFromHeader(principal);

        if (user == null) {
            throw new AccessDeniedException("Invalid WebSocket principal");
        }

        UUID userId = UUID.fromString(user.userId());

        boolean participant =
                chatAuthorizationService.isParticipant(
                        conversationId,
                        userId
                );

        if (!participant) {
            log.warn(
                    "Unauthorized conversation subscription: userId={}, conversationId={}",
                    userId,
                    conversationId
            );

            throw new AccessDeniedException(
                    "You are not a participant in this conversation"
            );
        }

        return message;
    }

    private UUID extractConversationId(String destination) {

        String prefix = "/topic/conversation/";

        String conversationId = destination.substring(prefix.length());

        try {
            return UUID.fromString(conversationId);
        } catch (IllegalArgumentException ex) {
            throw new AccessDeniedException(
                    "Invalid conversation ID"
            );
        }
    }
}
