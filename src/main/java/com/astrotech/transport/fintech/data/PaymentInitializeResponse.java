package com.astrotech.transport.fintech.data;

public record PaymentInitializeResponse(
        String authorizationUrl,
        // private String accessCode;
        String reference
) {

}
