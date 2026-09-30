package com.astrotech.transport.notifications;

import com.astrotech.transport.configProperties.AppProperties;
import com.astrotech.transport.configProperties.NotificationProperties;

import com.astrotech.transport.email.brevo.BrevoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotifications {


    private final NotificationProperties properties;
    private final AppProperties appProperties;

    private final BrevoClient brevoClient;

    public void sendVerificationEmail(
            String email,
            String otp,
            String token,
            String name) {

        String verifyLink = properties.frontendUrl()
                + "/verify-email.html?token="
                + token;

        var response = getVerifyHtmlAndText(name, otp, verifyLink);
        var html = response.html();
        var text = response.text();

        brevoClient.sendBrevoEmail(
                email,
                name,
                "Verify Your Email",
                html,
                text);
    }

    public void sendPasswordResetEmail(
            String email,
            String otp,
            String token,
            String name) {

        String resetLink = properties.frontendUrl()
                + "/reset-password?token="
                + token;

        var response = getResetHtmlAndText(name, otp, resetLink);
        var html = response.html();
        var text = response.text();

        brevoClient.sendBrevoEmail(
                email,
                name,
                "Reset Your Password",
                html,
                text);
    }

    public void sendPaymentSuccessEmail(
            String email,
            String name,
            String bookingCode,
            UUID paymentId) {

        String html = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2>Payment Successful</h2>
                
                    <p>Hello %s,</p>
                
                    <p>
                        Your payment has been successfully processed.
                    </p>
                
                    <p>
                        <strong>Booking Code:</strong> %s
                    </p>
                
                    <p>
                        <strong>Payment ID:</strong> %s
                    </p>
                
                    <p>
                        Thank you for choosing us.
                    </p>
                
                    <p>
                        Your bookings are now being processed.
                    </p>
                
                    <br>
                
                    <p>
                        Regards,<br>
                        Support Team
                    </p>
                
                </body>
                </html>
                """.formatted(
                name,
                bookingCode,
                paymentId);

        String text = """
                Hello %s,
                
                Your payment was successful.
                
                Booking Code: %s
                
                Payment ID: %s
                
                Your bookings are now being processed.
                
                Thank you for choosing us.
                """
                .formatted(
                        name,
                        bookingCode,
                        paymentId);

        brevoClient.sendBrevoEmail(
                email,
                name,
                "Payment Successful",
                html,
                text);
    }

    private EmailNotificationResponse getVerifyHtmlAndText(String name, String otp, String verifyLink) {

        String html;
        String text;
        if (Boolean.TRUE.equals(appProperties.enableSmsToken())) {
            html = """
                    <html>
                    <body style="font-family: Arial;">
                        <h2>Verify Your Email</h2>
                        <p>Hello %s,</p>
                        <p>Your OTP is:</p>
                        <h3>%s</h3>
                        <a href="%s">
                            Verify Email
                        </a>
                    </body>
                    </html>
                    """.formatted(name, otp, verifyLink);

            text = """
                    Hello %s,
                    
                    Your OTP is: %s
                    
                    Verify email:
                    %s
                    """.formatted(name, otp, verifyLink);

        } else {
            html = """
                    <html>
                    <body style="font-family: Arial;">
                        <h2>Verify Your Email</h2>
                        <p>Hello %s,</p>
                        <p>Your OTP is:</p>
                        <h3>%s</h3>
                    </body>
                    </html>
                    """.formatted(name, otp);

            text = """
                    Hello %s,
                    
                    Your OTP is: %s
                    
                    """.formatted(name, otp);
        }


        return new EmailNotificationResponse(
                html,
                text
        );
    }

    private EmailNotificationResponse getResetHtmlAndText(String name, String otp, String resetLink) {

        String html;
        String text;
        if (Boolean.TRUE.equals(appProperties.enableSmsToken())) {
            html = """
                    <html>
                    <body style="font-family: Arial;">
                        <h2>Password Reset</h2>
                        <p>Hello %s,</p>
                        <p>Your OTP is:</p>
                        <h3>%s</h3>
                        <a href="%s">
                            Reset Password
                        </a>
                    </body>
                    </html>
                    """.formatted(name, otp, resetLink);

            text = """
                    Hello %s,
                    
                    Your Password Reset OTP is: %s
                    
                    Reset password:
                    %s
                    """.formatted(name, otp, resetLink);

        } else {
            html = """
                    <html>
                    <body style="font-family: Arial;">
                        <h2>Password Reset</h2>
                        <p>Hello %s,</p>
                        <p>Your OTP is:</p>
                        <h3>%s</h3>
                    </body>
                    </html>
                    """.formatted(name, otp);

            text = """
                    Hello %s,
                    
                    Your Password Reset OTP is: %s
                    
                    
                    """.formatted(name, otp);
        }


        return new EmailNotificationResponse(
                html,
                text
        );
    }
}
