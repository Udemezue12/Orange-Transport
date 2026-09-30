package com.astrotech.transport.webhooks;

import com.astrotech.transport.dto.request.WebhookRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@Tag(name = "Payment Webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;


    @PostMapping("/paystack")
    public ResponseEntity<Void> handlePaystack(
            @RequestHeader(value = "x-paystack-signature", required = true) String signature,
            @RequestBody String rawJson) {
        return webhookService.verifyPaystackWebhook(signature, rawJson);
    }

    @PostMapping("/flutterwave")
    public ResponseEntity<Void> handleFlutterwave(
            @RequestHeader(value = "verif-hash", required = true) String verifHash,
            @RequestBody String rawJson) {
        return webhookService.verifyFlutterwaveWebhook(verifHash, rawJson);
    }
    @PostMapping("/monnify")
    public ResponseEntity<Void> handleMonnify(
            @RequestHeader(value = "monnify-signature", required = false) String monnifySignature,
            @RequestBody String rawJson) {
        return webhookService.verifyMonnifyWebhook(monnifySignature, rawJson);
    }

    @PostMapping("/stripe")
    public void handleWebhook(
            @RequestHeader Map<String, String> headers, @RequestBody String payload) {

        webhookService.handleWebhookEvent(new WebhookRequest(headers, payload));
    }
}
