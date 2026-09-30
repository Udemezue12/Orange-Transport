package com.astrotech.transport.configProperties;


import org.springframework.boot.context.properties.ConfigurationProperties;




@ConfigurationProperties(prefix = "payment")
public record PaymentProperties(
        String paystackSecretKey,
        String flutterwaveSecretKey,
        String flutterwaveSecretHash,
        String monnifyApiKey,
        String monnifySecretKey,
        String monnifyContractCode,
        String stripeSecretKey,
        String stripeWebhookSecretKey,
        String callbackUrl,
        String monnifyBaseUrl,
        String paystackBaseUrl,
        String flutterwaveBaseUrl
){



}
