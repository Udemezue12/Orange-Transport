package com.astrotech.transport.core;

import java.security.SecureRandom;

public class GenerateReference {


    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();


    public static String generateReference(String prefix) {
        StringBuilder reference = new StringBuilder();


        reference.append(prefix).append("-");


        String dateString = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
        reference.append(dateString).append("-");


        int length = 5;
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(CHARACTERS.length());
            reference.append(CHARACTERS.charAt(randomIndex));
        }

        return reference.toString();
    }
}
