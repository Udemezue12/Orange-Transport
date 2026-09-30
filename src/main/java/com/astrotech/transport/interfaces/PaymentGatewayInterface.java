package com.astrotech.transport.interfaces;

import com.astrotech.transport.fintech.data.PaymentInitializeResponse;
import com.astrotech.transport.fintech.data.PaymentRefundResponse;
import com.astrotech.transport.fintech.data.PaymentVerifyResponse;

import java.math.BigDecimal;

public interface PaymentGatewayInterface {
    PaymentInitializeResponse initializePayment(
            String email,
            String reference,
            BigDecimal amount,
            String callbackUrl);

    PaymentVerifyResponse verifyPayment(
            String reference);

    PaymentVerifyResponse webhookPaymentVerification(String reference);

    PaymentRefundResponse refundPayment(String transactionId, BigDecimal amount, String reason);
    PaymentRefundResponse verifyRefund(String refundId);

}
