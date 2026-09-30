package com.astrotech.transport.sms.termii;


import com.astrotech.transport.configProperties.NotificationProperties;
import com.astrotech.transport.core.MonoAndNormalizeSms;
import com.astrotech.transport.exceptions.NotificationException;
import com.astrotech.transport.messages.SendOtpAndLinkMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class TermiiClient {
    private final WebClient client;
    private final NotificationProperties properties;
    private final MonoAndNormalizeSms monoAndNormalizeSms;

    public TermiiClient(@Qualifier("termiiWebClient") WebClient client, NotificationProperties properties, MonoAndNormalizeSms monoAndNormalizeSms) {
        this.client = client;
        this.properties = properties;
        this.monoAndNormalizeSms = monoAndNormalizeSms;
    }

    public boolean ping() {

        try {

            Map<String, Object> payload = Map.of(
                    "to", "2340000000000",
                    "from", properties.termiiSenderId(),
                    "sms", "Ping test",
                    "type", "plain",
                    "channel", "generic",
                    "api_key", properties.termiiApiKey());

            TermiiSmsResponse response = client
                    .post()
                    .uri("/api/sms/send")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(TermiiSmsResponse.class)
                    .timeout(Duration.ofSeconds(15))
                    .block();

            return response != null;

        } catch (Exception ex) {

            log.error("Termii ping failed", ex);

            return false;
        }
    }

    public TermiiSmsResponse sendOtpSms(
            String to,
            String otp,
            String message,
            String name,
            String senderId) {

        var smsMessage = SendOtpAndLinkMessage.buildSmsMessage(
                otp,
                message,
                name);

        Map<String, Object> payload = Map.of(
                "to", monoAndNormalizeSms.normalizePhone(to),
                "from", senderId,
                "sms", smsMessage,
                "type", "plain",
                "channel", "generic",
                "api_key", properties.termiiApiKey());

        var response = client
                .post()
                .uri("/api/sms/send")
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Termii 4xx/5xx Response Body: {}", errorBody);
                                    return Mono.error(new NotificationException("Termii Error: " + errorBody));
                                })
                )
                .bodyToMono(TermiiSmsResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (response == null) {
            throw new NotificationException("Empty response from Termii");
        }

        return response;
    }
    public TermiiSmsResponse sendPaymentSuccessSms(
            String phoneNumber,
            String name,
            String bookingId,
            String senderId) {

        String smsMessage = """
                Hello %s,
                
                Your payment was successful.
                
                Booking Code: %s
                
                Your bookings are now being processed.
                
                Thank you for choosing us.
                """
                .formatted(name, bookingId);

        var payload = Map.of(
                "to",  monoAndNormalizeSms.normalizePhone(phoneNumber),
                "from", senderId,
                "sms", smsMessage,
                "type", "plain",
                "channel", "generic",
                "api_key", properties.termiiApiKey());

        var response = client
                .post()
                .uri("/api/sms/send")
                .bodyValue(payload)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        this::getMonoResponse)
                .bodyToMono(TermiiSmsResponse.class)
                .block();

        if (response == null) {
            throw new NotificationException(
                    "Empty response from Termii");
        }

        return response;
    }
    private Mono<? extends Throwable> getMonoResponse(ClientResponse clientResponse) {
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


}
