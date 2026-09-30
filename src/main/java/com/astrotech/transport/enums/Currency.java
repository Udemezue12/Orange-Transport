package com.astrotech.transport.enums;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum Currency {

    NGN("₦", "Nigerian Naira"),
    USD("$", "US Dollar"),
    EUR("€", "Euro"),
    GBP("£", "British Pound"),
    JPY("¥", "Japanese Yen"),
    CNY("¥", "Chinese Yuan"),
    INR("₹", "Indian Rupee"),
    ZAR("R", "South African Rand"),
    GHS("₵", "Ghanaian Cedi"),
    KES("KSh", "Kenyan Shilling"),
    EGP("E£", "Egyptian Pound"),
    CAD("$", "Canadian Dollar"),
    AUD("$", "Australian Dollar"),
    CHF("CHF", "Swiss Franc"),
    BRL("R$", "Brazilian Real"),
    MXN("$", "Mexican Peso"),
    RUB("₽", "Russian Ruble"),
    KRW("₩", "South Korean Won"),
    TRY("₺", "Turkish Lira"),
    SAR("﷼", "Saudi Riyal"),
    AED("د.إ", "UAE Dirham"),
    SEK("kr", "Swedish Krona"),
    NOK("kr", "Norwegian Krone"),
    DKK("kr", "Danish Krone"),
    PLN("zł", "Polish Zloty"),
    SGD("$", "Singapore Dollar"),
    IDR("Rp", "Indonesian Rupiah"),
    THB("฿", "Thai Baht"),
    VND("₫", "Vietnamese Dong"),
    PKR("₨", "Pakistani Rupee");

    private final String symbol;
    private final String displayName;

    public String getIsoCode() {
        return name();
    }

    public static Currency fromIsoCode(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            throw new IllegalArgumentException("Currency code cannot be null or blank");
        }

        try {
            return valueOf(isoCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unsupported currency: " + isoCode
            );
        }
    }
}
