package com.astrotech.transport.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
        @NotBlank(message = "Content is required")
        @Size(min = 10, max = 256, message = "Content should not exceed 256 characters")
        String content
) {
}
