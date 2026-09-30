package com.astrotech.transport.core;

import com.astrotech.transport.fintech.data.PaymentApiResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

public class FintechConstant {
    public static Integer toKobo(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    public static <T> ParameterizedTypeReference<PaymentApiResponse<T>> getParameters(Class<T> clazz) {

        var type = ResolvableType.forClassWithGenerics(
                PaymentApiResponse.class,
                clazz).getType();

        return ParameterizedTypeReference.forType(type);
    }

    public static BigDecimal fromKobo(Integer amount) {
        return BigDecimal.valueOf(amount)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static Duration getTimeout() {
        return Duration.ofSeconds(30);
    }

    public static Duration getMinuteTimeout(int timeout) {
        return Duration.ofMinutes(timeout);
    }
}
