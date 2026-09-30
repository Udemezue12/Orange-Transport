package com.astrotech.transport.fintech.data;

import java.math.BigDecimal;

public record PaymentRefundResponse(
        Boolean success,
        String status,
        String message,
        String refundId,
        String reference,
        BigDecimal amount,
        String currency
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean success;
        private String status;
        private String message;
        private String refundId;
        private String reference;
        private BigDecimal amount;
        private String currency;

        public Builder success(Boolean success) {
            this.success = success;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }


        public Builder id(Object id) {
            this.refundId = id != null ? String.valueOf(id) : null;
            return this;
        }

        public Builder refundId(String refundId) {
            this.refundId = refundId;
            return this;
        }

        public Builder reference(String reference) {
            this.reference = reference;
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

        public PaymentRefundResponse build() {
            return new PaymentRefundResponse(
                    success, status, message, refundId, reference, amount, currency
            );
        }
    }
}