package com.astrotech.transport.GatewaysController;

import com.astrotech.transport.configProperties.PaymentProperties;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.exceptions.ResourceNotFoundException;
import com.astrotech.transport.fintech.data.PaymentInitializeResponse;
import com.astrotech.transport.fintech.data.PaymentRefundResponse;
import com.astrotech.transport.fintech.data.PaymentVerifyResponse;
import com.astrotech.transport.interfaces.PaymentGatewayInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentGateway {
    private final Map<String, PaymentGatewayInterface> paymentGateways;
    private final PaymentProperties paymentProperties;

    public PaymentInitializeResponse initializeGateway(String paymentMethod, String reference, BigDecimal amount, String email) {

        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method must not be null or blank");
        }

        var paymentGateway = paymentGateways.get(paymentMethod);
        if (paymentGateway == null) {
            throw new ResourceNotFoundException("No Payment Service found for: " + paymentMethod);
        }


        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }
        var trimmedEmail = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(email, false);


        if (reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("Payment reference must not be null or blank");
        }


        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }


        String callbackUrl = paymentProperties != null ? paymentProperties.callbackUrl() : null;
        if (callbackUrl == null || callbackUrl.isBlank()) {
            throw new IllegalStateException("Payment callback URL is not configured in application properties");
        }

        return paymentGateway.initializePayment(
                trimmedEmail,
                reference,
                amount,
                callbackUrl
        );
    }

    public PaymentVerifyResponse verifyGateway(String reference, String paymentMethod) {
        var paymentGateway = paymentGateways.get(paymentMethod);
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method must not be null or blank");
        }
        return paymentGateway.verifyPayment(
                reference);
    }

    public PaymentVerifyResponse verifyWebhookGateway(String reference, String paymentMethod) {
        var paymentGateway = paymentGateways.get(paymentMethod);
        if (paymentGateway == null) {
            throw new ResourceNotFoundException("No Payment Service found");
        }
        return paymentGateway.webhookPaymentVerification(
                reference);
    }

    public PaymentRefundResponse refundGateway(String transactionId, String paymentMethod, BigDecimal amount, String reason) {
        var paymentGateway = paymentGateways.get(paymentMethod);
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method must not be null or blank");
        }
        return paymentGateway.refundPayment(
                transactionId, amount, reason);
    }
    public PaymentRefundResponse verifyRefundGateway(String refundId, String paymentMethod) {
        var paymentGateway = paymentGateways.get(paymentMethod);
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new IllegalArgumentException("Payment method must not be null or blank");
        }
        return paymentGateway.verifyRefund(
                refundId);
    }
}
