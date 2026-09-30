package com.astrotech.transport.enums;

import lombok.Getter;

@Getter
public enum PaymentMethod {

    PAYSTACK("PAYSTACK"),
    STRIPE("STRIPE"),
    FLUTTERWAVE("FLUTTERWAVE"),
    MONNIFY("MONNIFY");

    private final String prefix;

    PaymentMethod(String prefix) {
        this.prefix = prefix;
    }

}
