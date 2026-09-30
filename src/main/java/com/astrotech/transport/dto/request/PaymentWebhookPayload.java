package com.astrotech.transport.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentWebhookPayload(
        String event,
        @JsonAlias("event.type") String eventType,
        WebhookData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WebhookData(
            @JsonAlias({"reference", "tx_ref"}) String reference,
            String status,
            Long amount
    ) {
    }
}
