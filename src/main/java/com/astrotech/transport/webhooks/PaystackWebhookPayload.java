package com.astrotech.transport.webhooks;

public record PaystackWebhookPayload(
        String event,
        PaystackData data
) {
    public record PaystackData(String reference, String status, Long amount) {}
}
