package com.astrotech.transport.webhooks;

import com.astrotech.transport.configProperties.PaymentProperties;
import com.astrotech.transport.dto.request.WebhookRequest;
import com.astrotech.transport.fintech.foreignGateway.ForeignPaymentGateway;
import com.astrotech.transport.jobrunr.tasks.PaymentVerificationTask;
import com.astrotech.transport.service.BookingSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class WebhookService {


    private final PaymentVerificationTask paymentTask;
    private final JobScheduler jobScheduler;
    private final ObjectMapper objectMapper;
    private final PaymentProperties paymentConfig;
    private final ForeignPaymentGateway foreignPaymentGateway;
    private final BookingSessionService sessionService;

    public ResponseEntity<Void> verifyPaystackWebhook(String signature, String rawJsonRequestBody) {
        if (!WebhookValidator.isValidHmacSha512(rawJsonRequestBody, signature, paymentConfig.paystackSecretKey())) {
            log.warn("Unauthorized Paystack webhook attempt blocked!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            var payload = objectMapper.readValue(rawJsonRequestBody, PaystackWebhookPayload.class);

            if ("charge.success".equalsIgnoreCase(payload.event()) && payload.data() != null) {
                String reference = payload.data().reference();
                log.info("Processing Paystack payment verification for reference: {}", reference);
                jobScheduler.enqueue(() -> paymentTask.verifyPaymentUsingTransactionId(reference));
            }
        } catch (Exception e) {
            log.error("Failed to parse Paystack webhook body: {}", e.getMessage(), e);
        }


        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> verifyFlutterwaveWebhook(String signatureHeader, String rawJsonRequestBody) {
        if (!WebhookValidator.isEqual(signatureHeader, paymentConfig.flutterwaveSecretHash())) {
            log.warn("Unauthorized Flutterwave webhook attempt blocked!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            var payload = objectMapper.readValue(rawJsonRequestBody, FlutterwaveWebhookPayload.class);

            if (payload.event() != null && payload.event().contains("charge") && payload.data() != null) {
                String reference = payload.data().txRef();
                log.info("Processing Flutterwave payment verification for reference: {}", reference);
                jobScheduler.enqueue(() -> paymentTask.verifyPaymentUsingTransactionId(reference));
            }
        } catch (Exception e) {
            log.error("Failed to parse Flutterwave webhook body: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> verifyMonnifyWebhook(String monnifySignature, String rawJsonRequestBody) {

        if (!WebhookValidator.isValidMonnifySignature(rawJsonRequestBody, monnifySignature, paymentConfig.monnifySecretKey())) {
            log.warn("Unauthorized Monnify webhook attempt blocked!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            var payload = objectMapper.readValue(rawJsonRequestBody, MonnifyWebhookPayload.class);


            if ("SUCCESSFUL_TRANSACTION".equalsIgnoreCase(payload.eventType()) && payload.eventData() != null) {
                String reference = payload.eventData().paymentReference();
                log.info("Processing Monnify payment verification for reference: {}", reference);

                jobScheduler.enqueue(() -> paymentTask.verifyPaymentUsingTransactionId(reference));
            }
        } catch (Exception e) {
            log.error("Failed to parse Monnify webhook body: {}", e.getMessage(), e);
        }


        return ResponseEntity.ok().build();
    }

    public void handleWebhookEvent(WebhookRequest request) {
        foreignPaymentGateway.parseWebhookRequest(request).ifPresent(webhookResult -> sessionService.updateSessionStatus(webhookResult.bookingId(), webhookResult.status()));
    }

}
