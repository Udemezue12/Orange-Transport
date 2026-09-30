package com.astrotech.transport.jobrunr.tasks;
import com.astrotech.transport.core.TrimWhiteSpace;
import com.astrotech.transport.notifications.EmailNotifications;
import com.astrotech.transport.notifications.SmsNotification;
import com.astrotech.transport.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Component;

import java.util.UUID;
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentNotificationTask {

    private final EmailNotifications emailService;

    private final SmsNotification smsService;

    @Job(name = "Send Payment Successful email", retries = 3)
    public void sendPaymentSuccessNotificationEmail(String bookingCode,
                                                    String name,
                                                    String email,
                                                    UUID paymentId
    ) {
        var formattedName = TrimWhiteSpace.trimWhiteSpaceAndCapitalize(name);
        emailService.sendPaymentSuccessEmail(email, formattedName, bookingCode, paymentId);


    }

    @Job(name = "Send Payment Successful Sms", retries = 3)
    public void sendPaymentSuccessNotificationSms(
            String phoneNumber,
            String name,
            String bookingCode
    ) {
        var formattedName = TrimWhiteSpace.trimWhiteSpaceAndCapitalize(name);


        smsService.sendPaymentSuccessSms(phoneNumber,
                formattedName,
                bookingCode);

    }
}
