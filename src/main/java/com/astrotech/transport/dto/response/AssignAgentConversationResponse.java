package com.astrotech.transport.dto.response;

public record AssignAgentConversationResponse(
        SimpleChatConversationResponse conversationResponse,
        String passengerName,
        String agentName
){
    
}
