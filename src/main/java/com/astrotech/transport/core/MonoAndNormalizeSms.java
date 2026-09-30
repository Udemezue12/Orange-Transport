package com.astrotech.transport.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class MonoAndNormalizeSms {

    public Mono<? extends Throwable> getMonoResponse(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(this::getMono);
    }

    private Mono<? extends Throwable> getMono(String errorBody) {
        log.error("Termii Error: {}", errorBody);
        return Mono.error(
                new RuntimeException(
                        "Termii API Error: "
                                + errorBody));
    }

    public String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String normalized = phone.trim()
                .replaceAll("[\\s()-]", "");

        if (normalized.startsWith("+234")) {

        } else if (normalized.startsWith("234")) {
            normalized = "+" + normalized;
        } else if (normalized.startsWith("0")) {
            normalized = "+234" + normalized.substring(1);
        } else if (normalized.matches("\\d{10}")) {
            normalized = "+234" + normalized;
        }

        if (!normalized.matches("^\\+234\\d{10}$")) {
            throw new IllegalArgumentException(
                    "Invalid Nigerian phone number: " + phone
            );
        }

        return normalized;
    }
}
