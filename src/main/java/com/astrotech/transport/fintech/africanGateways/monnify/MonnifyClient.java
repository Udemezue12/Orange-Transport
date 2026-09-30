package com.astrotech.transport.fintech.africanGateways.monnify;


import com.astrotech.transport.configProperties.PaymentProperties;
import com.astrotech.transport.core.FintechConstant;
import com.astrotech.transport.enums.PaymentMethod;
import com.astrotech.transport.exceptions.PaymentException;
import com.astrotech.transport.fintech.data.PaymentInitializeResponse;
import com.astrotech.transport.fintech.data.PaymentRefundResponse;
import com.astrotech.transport.fintech.data.PaymentVerifyResponse;
import com.astrotech.transport.interfaces.PaymentGatewayInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;


@Service("monnify")
@Slf4j
public class MonnifyClient implements PaymentGatewayInterface {
    private final WebClient monnifyClient;
    private final PaymentProperties paymentProperties;

    private final Duration timeout = FintechConstant.getTimeout();

    public MonnifyClient(@Qualifier("monnifyWebClient") WebClient monnifyClient, PaymentProperties paymentProperties) {
        this.monnifyClient = monnifyClient;
        this.paymentProperties = paymentProperties;

    }

    private String getAccessToken() {
        var credentials = Base64.getEncoder().encodeToString((paymentProperties.monnifyApiKey() + ":" + paymentProperties.monnifySecretKey()).getBytes());
//        var getParameters = FintechConstant.getParameters(MonnifyAuthData.class);

        var response = monnifyClient.post()
                .uri("/api/v1/auth/login")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<MonnifyApiResponse<MonnifyAuthData>>() {
                })
                .timeout(timeout)
                .block();

        if (response == null || !response.requestSuccessful()) {
            throw new PaymentException("Failed to authenticate with Monnify: " +
                    (response != null ? response.responseMessage() : "Empty response"));
        }

        return response.responseBody().accessToken();
    }

    @Override
    public PaymentInitializeResponse initializePayment(String email, String reference, BigDecimal amount, String callbackUrl) {
        var token = getAccessToken();
        try {
            var payload = Map.of(
                    "amount", amount,
                    "customerName", email.split("@")[0],
                    "customerEmail", email,
                    "paymentReference", reference,
                    "paymentDescription", "Payment for ticket order " + reference,
                    "currencyCode", "NGN",
                    "contractCode", paymentProperties.monnifyContractCode(),
                    "redirectUrl", callbackUrl,
                    "paymentMethods", List.of("CARD", "ACCOUNT_TRANSFER")
            );



            var response = monnifyClient.post()
                    .uri("/api/v1/merchant/transactions/init-transaction")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<MonnifyApiResponse<MonnifyInitializeData>>() {
                    })
                    .timeout(timeout)
                    .block();
            validateResponse(response);

            var data = response.responseBody();
            return new PaymentInitializeResponse(data.checkoutUrl(), data.paymentReference());
        } catch (WebClientResponseException e) {
            log.error("Monnify 400 Bad Request Payload Error: {}", e.getResponseBodyAsString());
            throw e;
        }


    }

    @Override
    public PaymentVerifyResponse verifyPayment(String reference) {
        var token = getAccessToken();

        var response = monnifyClient.get()

                .uri(uriBuilder -> uriBuilder
                        .path("/api/v2/merchant/transactions/query")
                        .queryParam("paymentReference", reference)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<MonnifyApiResponse<MonnifyVerifyData>>() {})
                .timeout(timeout)
                .block();

        validateResponse(response);

        var tx = response.responseBody();

        return PaymentVerifyResponse.builder()
                .success("PAID".equalsIgnoreCase(tx.paymentStatus()))
                .gateway(PaymentMethod.MONNIFY.toString())
                .status(tx.paymentStatus())
                .transactionId(tx.transactionReference())
                .reference(tx.paymentReference())
                .amount(tx.amountPaid() != null ? tx.amountPaid() : tx.totalPayable())
                .currency(tx.currencyCode())
                .metadata(tx.metaData())
                .paidAt(tx.paidOn())
                .channel(tx.paymentMethod())
                .customer(tx.customer())
                .build();
    }

    @Override
    public PaymentVerifyResponse webhookPaymentVerification(String reference) {
        return verifyPayment(reference);
    }

    @Override
    public PaymentRefundResponse refundPayment(String transactionId, BigDecimal amount, String reason) {
        var token = getAccessToken();

        var payload = Map.of(
                "transactionReference", transactionId,
                "refundAmount", amount,
                "refundReason", reason,
                "customerNote", reason
        );

        var response = monnifyClient.post()
                .uri("/api/v1/dispute/refund")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<MonnifyApiResponse<MonnifyRefundData>>() {
                })
                .timeout(timeout)
                .block();

        validateResponse(response);

        var data = response.responseBody();

        return PaymentRefundResponse.builder()
                .success("COMPLETED".equalsIgnoreCase(data.status())
                        || "SUCCESS".equalsIgnoreCase(data.status()))
                .status(data.status())
                .id(data.refundReference())
                .reference(data.paymentReference())
                .amount(data.amount() != null
                        ? data.amount()
                        : amount)
                .currency("NGN")
                .message(data.message() != null
                        ? data.message()
                        : response.responseMessage())
                .build();
    }
    @Override
    public PaymentRefundResponse verifyRefund(String refundId) {

        var token = getAccessToken();

        var response = monnifyClient.get()
                .uri(
                        "/api/v1/dispute/refund" +
                                "/{transactionReference}",
                        refundId
                )
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + token
                )
                .retrieve()
                .bodyToMono(
                        new ParameterizedTypeReference<
                                MonnifyApiResponse<MonnifyRefundData>
                                >() {
                        }
                )
                .timeout(timeout)
                .block();

        validateResponse(response);

        var data = response.responseBody();

        return PaymentRefundResponse.builder()
                .success(
                        "COMPLETED".equalsIgnoreCase(data.status())
                                || "SUCCESS".equalsIgnoreCase(data.status())
                )
                .status(data.status())
                .id(data.refundReference())
                .reference(data.paymentReference())
                .amount(data.amount())
                .currency("NGN")
                .message(
                        data.message() != null
                                ? data.message()
                                : response.responseMessage()
                )
                .build();
    }

    private void validateResponse(MonnifyApiResponse<?> response) {
        if (response == null) {
            throw new PaymentException("Empty response from Monnify");
        }

        if (!response.requestSuccessful()) {
            throw new PaymentException(response.responseMessage());
        }
    }
}
