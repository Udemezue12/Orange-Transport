package com.astrotech.transport.dto.request;

import java.util.Map;

public record WebhookRequest(
        Map<String, String> signature,
        String payload
) {
}
