package com.astrotech.transport.core;

import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;

@RequiredArgsConstructor
public final class RegistrationCodeGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final char[] ALPHANUMERIC =
            "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();



    public static String generate() {
        return "ORANGE-" +
                randomBlock(4) + "-" +
                randomBlock(5) + "-" +
                randomBlock(4);
    }

    private static String randomBlock(int requiredLength) {
        StringBuilder builder = new StringBuilder(requiredLength);

        for (int i = 0; i < requiredLength; i++) {
            builder.append(ALPHANUMERIC[RANDOM.nextInt(ALPHANUMERIC.length)]);
        }

        return builder.toString();
    }
}
