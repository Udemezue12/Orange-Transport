package com.astrotech.transport.webhooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MonnifyWebhookPayload(
        String eventType,
        MonnifyEventData eventData
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MonnifyEventData(
            String transactionReference,
            String paymentReference,
            String paymentStatus,
            Double amountPaid,
            String paidOn
    ) {}
}
