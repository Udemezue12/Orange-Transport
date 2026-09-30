package com.astrotech.transport.email.brevo;

import com.astrotech.transport.configProperties.NotificationProperties;
import com.astrotech.transport.core.NotificationCircuitBreaker;
import com.astrotech.transport.exceptions.NotificationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class BrevoClient {
    private final WebClient client;
    private final NotificationCircuitBreaker breaker;
    private final NotificationProperties properties;


    public BrevoClient(NotificationCircuitBreaker breaker, @Qualifier("brevoWebClient") WebClient client, NotificationProperties properties) {
        this.breaker = breaker;
        this.client = client;
        this.properties = properties;
    }

    private void sendEmail(
            BrevoEmailRequest request) {

        var response = client
                .post()
                .uri("")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BrevoEmailResponse.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (response == null) {
            throw new NotificationException(
                    "Empty response from Brevo");
        }

    }

    public void sendBrevoEmail(
            String email,
            String name,
            String subject,
            String html,
            String text) {


        breaker.execute(() -> {

            var request = BrevoEmailRequest.builder()
                    .sender(
                            BrevoEmailRequest.Sender.builder()
                                    .name(properties.projectName())
                                    .email(
                                            properties.emailUsername())
                                    .build())
                    .to(List.of(
                            BrevoEmailRequest.Recipient
                                    .builder()
                                    .email(email)
                                    .name(name)
                                    .build()))
                    .subject(subject)
                    .htmlContent(html)
                    .textContent(null)
                    .build();

            sendEmail(request);

            return true;
        });
    }

}
