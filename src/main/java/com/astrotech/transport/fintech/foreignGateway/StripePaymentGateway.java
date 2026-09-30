package com.astrotech.transport.fintech.foreignGateway;
import com.astrotech.transport.configProperties.NotificationProperties;
import com.astrotech.transport.configProperties.PaymentProperties;
import com.astrotech.transport.dto.request.WebhookRequest;
import com.astrotech.transport.dto.response.WebhookPaymentStatus;
import com.astrotech.transport.entities.BookingSession;
import com.astrotech.transport.enums.BookingSessionStatus;
import com.astrotech.transport.exceptions.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentGateway implements ForeignPaymentGateway {
    private final NotificationProperties notificationProperties;
    private final PaymentProperties paymentProperties;

    @Override
    public CheckOutSession createCheckSession(BookingSession booking) {

        try {
            var frontendUrl = notificationProperties.frontendUrl();
            var sessionBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(frontendUrl + "/checkout-success.html?orderId=" + booking.getId())
                    .setCancelUrl(frontendUrl + "/checkout-cancel.html")
                    .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder().putMetadata("booking_id", booking.getId().toString()).build());
//            order.getItems().forEach(item -> {
//                sessionBuilder.addLineItem(
//                        SessionCreateParams.LineItem.builder()
//                                .setQuantity(Long.valueOf(item.getQuantity()))
//                                .setPriceData(
//                                        getLineData(item)
//                                )
//                                .build()
//                );
//            });
            var session = Session.create(sessionBuilder.build());
            return new CheckOutSession(session.getUrl(), session.getClientReferenceId()
            );
        } catch (StripeException e) {
            log.error("Stripe checkout failed", e);
            throw new PaymentException("Stripe checkout failed: " + e);

        }

    }

    @Override
    public Optional<WebhookPaymentStatus> parseWebhookRequest(WebhookRequest request) {
        try {
            var payload = request.payload();
            var signature = request.signature().get("Stripe-signature");
            var event = Webhook.constructEvent(payload, signature, paymentProperties.stripeWebhookSecretKey());

            return switch (event.getType()) {
                case "payment_intent.succeeded" ->
                        Optional.of(new WebhookPaymentStatus(extractBookingId(event), BookingSessionStatus.COMPLETED));
                case "payment_intent.payment_failed" ->
                        Optional.of(new WebhookPaymentStatus(extractBookingId(event), BookingSessionStatus.CANCELLED));
                default -> Optional.empty();
            };

        } catch (SignatureVerificationException e) {
            throw new PaymentException("Invalid Signature");
        }
    }

    private UUID extractBookingId(Event event) {
        var stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(() -> new PaymentException("Cannot Deserialize this, check API Version"));
        var paymentIntent = (PaymentIntent) stripeObject;
        return UUID.fromString(paymentIntent.getMetadata().get("booking_id"));
    }

//    private SessionCreateParams.LineItem.PriceData getLineData(OrderItem item) {
//        return SessionCreateParams.LineItem.PriceData.builder()
//                .setCurrency("usd")
//                .setUnitAmount(
//                        item.getUnitPrice()
//                                .multiply(BigDecimal.valueOf(100))
//                                .longValue()
//                )
//                .setProductData(
//                        getProductData(item)
//                )
//                .build();
//    }

//    private SessionCreateParams.LineItem.PriceData.ProductData getProductData(OrderItem item) {
//        return SessionCreateParams.LineItem.PriceData.ProductData.builder()
//                .setName(item.getProduct().getName())
//                .build();
//    }
}
