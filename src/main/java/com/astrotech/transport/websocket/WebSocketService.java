package com.astrotech.transport.websocket;

import com.astrotech.transport.dto.request.*;
import com.astrotech.transport.dto.response.*;
import com.astrotech.transport.enums.*;
import com.astrotech.transport.events.TypingEvent;
import com.astrotech.transport.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@RequiredArgsConstructor
@Slf4j
@Service
public class WebSocketService {


    private final ChatMessageService chatService;

    private final UserService userService;

    private final SimpMessagingTemplate simpMessagingTemplate;


    public void sendMessage(String conversationId, SendChatMessageRequest request,
                            SimpMessageHeaderAccessor headers) {
        log.info("WebSocketService reached");
        var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

        if (user == null) {

            sendError(headers, "UNAUTHORIZED", "Not authenticated", request.content());

            return;

        }

        if (conversationId == null || conversationId.isBlank()) {

            sendError(headers, "INVALID_PAYLOAD", "conversationId is required", request.content());

            return;

        }
        var content = request != null ? request.content() : null;


        if (content == null || content.isBlank()) {
            sendError(headers, "BAD_REQUEST", "Request content cannot be empty", content);
            return;
        }

        int length = content.length();


        if (length < 10 || length > 256) {
            sendError(headers, "NOT_ACCEPTED", "Content length must be between 10 and 256 characters", content);
            return;
        }


        try {


            var response = chatService.createMessage(
                    UUID.fromString(conversationId),
                    UUID.fromString(user.userId()),
                    request
            );

            if (response.sent()) {
                simpMessagingTemplate.convertAndSend(
                        "/topic/conversation/" + conversationId,
                        response
                );
            }


        } catch (Exception e) {

            log.error("WS sendMessage error: userId={} convId={}", user.userId(), conversationId, e);


            sendError(headers, "SEND_FAILED", e.getMessage(), request.content());

        }

    }

    public void markMessageAsDelivered(String messageId, SimpMessageHeaderAccessor headers) {
        var extractUser = WebSocketUtils.extractUserFromHeader(headers.getUser());
        if (extractUser == null) return;
        var userId = UUID.fromString(extractUser.userId());

        var user = userService.findUserId(userId, UserStatus.ACTIVE).orElse(null);


        if (user == null || messageId == null) return;
        chatService.markMessageDelivered(messageId, userId);
    }


    public void typing(String conversationId,

                       SimpMessageHeaderAccessor headers) {

        var extractUser = WebSocketUtils.extractUserFromHeader(headers.getUser());
        if (extractUser == null) return;
        var user = userService.findUserId(UUID.fromString(extractUser.userId()), UserStatus.ACTIVE).orElse(null);


        if (user == null || conversationId == null) return;


        var event = new TypingEvent(

                user.getId(),
                user.getFullName(),

                conversationId,

                true,

                Instant.now());


        simpMessagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                event
        );

    }


    public Map<String, Object> ping(SimpMessageHeaderAccessor headers) {

        log.info("PING RECEIVED");

        var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

        log.info("USER = {}", user);

        if (user != null) {

            userService.updateOnlineStatus(user.userId(), OnlineStatus.ONLINE);
        }


        return Map.of("pong", true, "ts", Instant.now().toString());

    }

    private void sendError(
            SimpMessageHeaderAccessor headers,
            String code,
            String message,
            String ref) {

        var user = WebSocketUtils.extractUserFromHeader(headers.getUser());

        if (user == null) {
            log.error("Failed to send WebSocket error: User context is null. Code: {}, Message: {}, Ref: {}", code, message, ref);
            return;
        }
        var newMessage = message != null ? message : "Unknown error";

        log.error("WebSocket error for user [{}]: Code: {}, Message: {}, Ref: {}", user.userId(), code, newMessage, ref);

        simpMessagingTemplate.convertAndSendToUser(
                user.userId(),
                "/queue/errors",
                Map.of(
                        "code", code,
                        "message", newMessage,
                        "ref", ref != null ? ref : ""
                )
        );
    }


}
