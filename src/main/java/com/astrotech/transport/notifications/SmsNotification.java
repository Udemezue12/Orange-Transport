package com.astrotech.transport.notifications;

import com.astrotech.transport.configProperties.NotificationProperties;
import com.astrotech.transport.sms.termii.TermiiClient;
import com.astrotech.transport.sms.termii.TermiiSmsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsNotification {

    private final TermiiClient termiiClient;

    private final NotificationProperties properties;

    public void sendOtp(
            String phone,
            String otp,
            String name) {

        termiiClient.sendOtpSms(
                phone,
                otp,
                null,
                name,
                properties.termiiSenderId());
    }

    public TermiiSmsResponse sendCustomSms(
            String phone,
            String message) {

        return termiiClient.sendOtpSms(
                phone,
                null,
                message,
                null,
                properties.termiiSenderId());
    }
    public void sendPaymentSuccessSms(String phoneNumber,
                                      String name,
                                      String orderId) {
        termiiClient.sendPaymentSuccessSms(
                phoneNumber,
                name,
                orderId,
                properties.termiiSenderId()
        );

    }


}
