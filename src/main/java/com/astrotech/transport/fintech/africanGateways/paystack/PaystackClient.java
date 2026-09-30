package com.astrotech.transport.fintech.africanGateways.paystack;

import com.astrotech.transport.enums.PaymentMethod;
import com.astrotech.transport.exceptions.PaymentException;
import com.astrotech.transport.core.FintechConstant;
import com.astrotech.transport.fintech.data.PaymentApiResponse;
import com.astrotech.transport.fintech.data.PaymentInitializeResponse;
import com.astrotech.transport.fintech.data.PaymentRefundResponse;
import com.astrotech.transport.fintech.data.PaymentVerifyResponse;
import com.astrotech.transport.interfaces.PaymentGatewayInterface;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Service("paystack")
public class PaystackClient implements PaymentGatewayInterface {

    private final WebClient paystackClient;

    private final Duration timeout = FintechConstant.getTimeout();

    public PaystackClient(@Qualifier("paystackWebClient") WebClient paystackClient) {
        this.paystackClient = paystackClient;

    }

    @Override
    public PaymentInitializeResponse initializePayment(String email, String reference, BigDecimal amount,
                                                       String callbackUrl) {

        var final_amount = FintechConstant.toKobo(amount);
        var getParameters = FintechConstant.getParameters(PaystackInitializeData.class);


        var payload = Map.of(
                "email", email,
                "amount", final_amount,
                "reference", reference,
                "callback_url", callbackUrl);

        var elementTypeRef = new ParameterizedTypeReference<PaymentApiResponse<PaystackInitializeData>>() {
        };
        var response = paystackClient.post()
                .uri("/transaction/initialize")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(getParameters)
                .timeout(timeout)
                .block();

        validateResponse(response);

        var data = response.getData();
        return new PaymentInitializeResponse(data.authorizationUrl(), data.reference());
    }

    @Override
    public PaymentVerifyResponse verifyPayment(String reference) {
        var getParameters = FintechConstant.getParameters(PaystackVerifyData.class);
        var response = paystackClient.get()
                .uri("/transaction/verify/{reference}", reference)
                .retrieve()
                .bodyToMono(getParameters)
                .timeout(timeout)
                .block();

        validateResponse(response);

        var tx = response.getData();
        var final_amount = FintechConstant.fromKobo(tx.amount());

        return PaymentVerifyResponse.builder()
                .success(response.isStatus())
                .gateway(PaymentMethod.PAYSTACK.toString())
                .status(tx.status())
                .transactionId(String.valueOf(tx.id()))
                .reference(tx.reference())
                .amount(final_amount)
                .currency(tx.currency())
                .metadata(tx.metadata())
                .paidAt(tx.paidAt())
                .channel(tx.channel())
                .customer(tx.customer())
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
        var amountInKobo = FintechConstant.toKobo(amount);

        var payload = Map.of(
                "transaction", transactionId,
                "amount", amountInKobo,
                "merchant_note", reason
        );

        var getParameters =
                FintechConstant.getParameters(PaystackRefundData.class);

        var response = paystackClient.post()
                .uri("/refund")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(getParameters)
                .timeout(timeout)
                .block();

        validateResponse(response);

        var data = response.getData();

        var refundedAmount = data.amount() != null
                ? FintechConstant.fromKobo(data.amount())
                : amount;

        return PaymentRefundResponse.builder()
                .success(data.success() != null ? data.success() : true)
                .status(data.status())
                .id(data.id())
                .reference(
                        data.transaction() != null
                                ? data.transaction().reference()
                                : data.transactionReference()
                )
                .amount(refundedAmount)
                .currency(data.currency())
                .message(response.getMessage())
                .build();
    }
    @Override
    public PaymentRefundResponse verifyRefund(String refundId) {

        var getParameters =
                FintechConstant.getParameters(PaystackRefundData.class);

        var response = paystackClient.get()
                .uri("/refund/{id}", refundId)
                .retrieve()
                .bodyToMono(getParameters)
                .timeout(timeout)
                .block();

        validateResponse(response);

        var data = response.getData();

        var refundedAmount = data.amount() != null
                ? FintechConstant.fromKobo(data.amount())
                : null;

        return PaymentRefundResponse.builder()
                .success(data.success() != null ? data.success() : true)
                .status(data.status())
                .id(data.id())
                .reference(
                        data.transaction() != null
                                ? data.transaction().reference()
                                : data.transactionReference()
                )
                .amount(refundedAmount)
                .currency(data.currency())
                .message(response.getMessage())
                .build();
    }


    private void validateResponse(PaymentApiResponse<?> response) {

        if (response == null) {
            throw new PaymentException("Empty response from Paystack");
        }

        if (!response.isStatus()) {
            throw new PaymentException(response.getMessage());
        }
    }

}
