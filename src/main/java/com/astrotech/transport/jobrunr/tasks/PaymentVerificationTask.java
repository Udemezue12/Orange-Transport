package com.astrotech.transport.jobrunr.tasks;

import com.astrotech.transport.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentVerificationTask {


    private final PaymentService paymentService;





    @Job(name = "verify-payment-for-reference %0", retries = 3)
    public void verifyPaymentUsingTransactionId(String incomingReference) {
        paymentService.webhookVerifyPayment(incomingReference);

    }
}
