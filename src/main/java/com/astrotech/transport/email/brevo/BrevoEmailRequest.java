package com.astrotech.transport.email.brevo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record BrevoEmailRequest(
        @JsonProperty("sender") Sender sender,
        @JsonProperty("to") List<Recipient> to,
        @JsonProperty("subject") String subject,
        @JsonProperty("htmlContent") String htmlContent,
        @JsonProperty("textContent") String textContent
) {
    @Builder
    public record Sender(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email
    ) {}

    @Builder
    public record Recipient(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email
    ) {}
}
