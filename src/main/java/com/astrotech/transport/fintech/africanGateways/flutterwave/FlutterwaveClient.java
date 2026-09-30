package com.astrotech.transport.fintech.africanGateways.flutterwave;

import com.astrotech.transport.fintech.data.PaymentApiResponse;
import com.astrotech.transport.fintech.data.PaymentInitializeResponse;
import com.astrotech.transport.fintech.data.PaymentRefundResponse;
import com.astrotech.transport.fintech.data.PaymentVerifyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.astrotech.transport.enums.PaymentMethod;
import com.astrotech.transport.exceptions.BadRequestException;
import com.astrotech.transport.exceptions.PaymentException;
import com.astrotech.transport.core.FintechConstant;
import com.astrotech.transport.interfaces.PaymentGatewayInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Service("flutterwave")
public class FlutterwaveClient implements PaymentGatewayInterface {
    private final WebClient flutterwaveClient;
    private final ObjectMapper objectMapper;
    private final Duration timeout = FintechConstant.getTimeout();

    public FlutterwaveClient(@Qualifier("flutterwaveWebClient") WebClient flutterwaveClient,
                             ObjectMapper objectMapper) {
        this.flutterwaveClient = flutterwaveClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentInitializeResponse initializePayment(
            String email,
            String reference,
            BigDecimal amount,
            String callbackUrl) {

        var payload = Map.of(
                "tx_ref", reference,
                "amount", amount,
                "currency", "NGN",
                "redirect_url", callbackUrl,
                "customer", Map.of("email", email),
                "payment_options", "card",
                "customizations", Map.of(
                        "title", "Online Payment",
                        "description", "Online Payment"));
        var getParameters = new ParameterizedTypeReference<PaymentApiResponse<FlutterwaveInitializeData>>() {
        };

        var response = flutterwaveClient.post()
                .uri("/payments")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(getParameters)
                .timeout(timeout)
                .block();

        validateResponse(response);

        return new PaymentInitializeResponse(
                response.getData().link(),
                reference);
    }

    @Override
    public PaymentVerifyResponse verifyPayment(String reference) {
        var getParameters = new ParameterizedTypeReference<PaymentApiResponse<FlutterwaveVerifyData>>() {
        };
        var response = flutterwaveClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/transactions/verify_by_reference")
                        .queryParam("tx_ref", reference)
                        .build())
                .retrieve()
                .bodyToMono(getParameters)
                .timeout(timeout)
                .block();

        validateResponse(response);

        var tx = response.getData();

        if (!"successful".equalsIgnoreCase(tx.status())) {
            return PaymentVerifyResponse.builder()
                    .success(false)
                    .gateway(PaymentMethod.FLUTTERWAVE.toString())
                    .status(tx.status())
                    .txRef(tx.txRef())
                    .build();
        }

        return PaymentVerifyResponse.builder()
                .success(true)
                .gateway(PaymentMethod.FLUTTERWAVE.toString())
                .transactionId(String.valueOf(tx.id()))
                .txRef(tx.txRef())
                .flwRef(tx.flwRef())
                .amount(tx.amount())
                .currency(tx.currency())
                .status(tx.status())
                .createdAt(tx.createdAt())
                .customer(tx.customer())
                .meta(tx.meta())
                .build();
    }

    @Override
    public PaymentVerifyResponse webhookPaymentVerification(String reference) {
        return verifyPayment(reference);

    }

    @Override
    public PaymentRefundResponse refundPayment(
            String transactionId,
            BigDecimal amount,
            String reason
    ) {
        var getParameters =
                FintechConstant.getParameters(FlutterwaveRefundData.class);

        var refundPayload = Map.of(
                "amount", amount,
                "comments", reason
        );

        var response = flutterwaveClient.post()
                .uri("/transactions/{id}/refund", transactionId)
                .bodyValue(refundPayload)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse
                                .bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(
                                            "CRITICAL FLUTTERWAVE ERROR BODY: {}",
                                            errorBody
                                    );

                                    return Mono.error(
                                            new BadRequestException(
                                                    "Flutterwave validation failed: "
                                                            + errorBody
                                            )
                                    );
                                })
                )
                .bodyToMono(getParameters)
                .timeout(timeout)
                .block();

        validateResponse(response);

        if (response.getData() == null) {
            throw new BadRequestException(
                    "Flutterwave refund response contains no data"
            );
        }

        var data = response.getData();

        var success = data.success() != null
                ? data.success()
                : response.isStatus();

        return PaymentRefundResponse.builder()
                .success(success)
                .amount(data.amountRefunded())
                .refundId(String.valueOf(data.id()))
                .status(data.status())
                .message(response.getMessage())
                .build();
    }

    @Override
    public PaymentRefundResponse verifyRefund(String refundId) {

        var getParameters =
                FintechConstant.getParameters(FlutterwaveRefundData.class);

        var response = flutterwaveClient.get()
                .uri("/transactions/{id}/refund", refundId)
                .retrieve()
                .bodyToMono(getParameters)
                .timeout(timeout)
                .block();

        validateResponse(response);

        if (response.getData() == null) {
            throw new BadRequestException(
                    "Flutterwave refund verification response contains no data"
            );
        }

        var data = response.getData();

        var success = data.success() != null
                ? data.success()
                : response.isStatus();

        return PaymentRefundResponse.builder()
                .success(success)
                .amount(data.amountRefunded())
                .refundId(String.valueOf(data.id()))
                .status(data.status())
                .message(response.getMessage())
                .build();
    }

    private void validateResponse(
            PaymentApiResponse<?> response) {

        if (response == null) {
            throw new PaymentException(
                    "Empty response from Flutterwave");
        }

        if (!"success".equalsIgnoreCase(response.isStatus() ? "success" : "error")) {
            throw new PaymentException(response.getMessage());
        }
    }

}
