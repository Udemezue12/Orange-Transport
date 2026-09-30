package com.astrotech.transport.websocket;


import com.astrotech.transport.dto.response.SendChatMessageRequest;
import com.astrotech.transport.ratelimit.redisRatelimit.Ratelimit;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;


@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final WebSocketService webSocketService;


    @MessageMapping("/chat.sendMessage/{conversationId}")
    @Ratelimit
    public void sendMessage(@DestinationVariable String conversationId, @Valid @Payload SendChatMessageRequest payload,
                            SimpMessageHeaderAccessor headers) {
        log.info("Send Message Controller reached");
        webSocketService.sendMessage(conversationId, payload, headers);
    }

    @MessageMapping("/chat.typing/{conversationId}")
    @Ratelimit
    public void typing(@DestinationVariable String conversationId,
                       SimpMessageHeaderAccessor headers) {
        webSocketService.typing(conversationId, headers);
    }


    @MessageMapping("/chat.ping")
    @SendToUser("/queue/pong")
    @Ratelimit
    public Map<String, Object> ping(SimpMessageHeaderAccessor headers) {
        return webSocketService.ping(headers);
    }


}
