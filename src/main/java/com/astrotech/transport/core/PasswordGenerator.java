package com.astrotech.transport.core;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final String LOWERCASE = "abcdefghijkmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}<>?";



    private static final String ALL_CHARACTERS =
            LOWERCASE + UPPERCASE + DIGITS + SPECIAL;

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    private final SecureRandom random = new SecureRandom();

    public String generate(int length) {

        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Password length must be between "
                            + MIN_LENGTH + " and " + MAX_LENGTH + " characters.");
        }

        char[] password = new char[length];


        password[0] = randomCharacter(LOWERCASE);
        password[1] = randomCharacter(UPPERCASE);
        password[2] = randomCharacter(DIGITS);
        password[3] = randomCharacter(SPECIAL);


        for (int i = 4; i < length; i++) {
            password[i] = randomCharacter(ALL_CHARACTERS);
        }


        shuffle(password);

        return new String(password);
    }


    public String generate() {
        return generate(12);
    }

    private char randomCharacter(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    private void shuffle(char[] array) {

        for (int i = array.length - 1; i > 0; i--) {

            int j = random.nextInt(i + 1);

            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}