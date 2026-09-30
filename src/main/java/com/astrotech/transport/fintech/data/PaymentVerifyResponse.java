package com.astrotech.transport.fintech.data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.fasterxml.jackson.annotation.*;
public record PaymentVerifyResponse(
        boolean success,
        String gateway,
        String transactionId,
        String reference,
        String gatewayReference,
        BigDecimal amount,
        @JsonProperty("currency")
        @JsonAlias({"currencyCode", "currency"})
        String currency,
        String status,
        String channel,
        OffsetDateTime paidAt,
        Object customer,
        Object metadata
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String gateway;
        private String transactionId;
        private String reference;
        private String gatewayReference;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String channel;
        private OffsetDateTime paidAt;
        private Object customer;
        private Object metadata;

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder gateway(String gateway) {
            this.gateway = gateway;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }


        public Builder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder txRef(String txRef) {
            this.reference = txRef;
            return this;
        }

        public Builder flwRef(String flwRef) {
            this.gatewayReference = flwRef;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }
        public Builder currencyCode(String currencyCode) {
            this.currency = currencyCode;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder channel(String channel) {
            this.channel = channel;
            return this;
        }


        public Builder paidAt(OffsetDateTime paidAt) {
            this.paidAt = paidAt;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.paidAt = createdAt;
            return this;
        }

        public Builder customer(Object customer) {
            this.customer = customer;
            return this;
        }


        public Builder metadata(Object metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder meta(Object meta) {
            this.metadata = meta;
            return this;
        }

        public PaymentVerifyResponse build() {

            var finalCurrency = (currency != null && !currency.isBlank()) ? currency : "NGN";

            return new PaymentVerifyResponse(
                    success, gateway, transactionId, reference, gatewayReference,
                    amount, finalCurrency, status, channel, paidAt, customer, metadata
            );
        }
    }
}
