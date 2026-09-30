package com.astrotech.transport.util;


import com.astrotech.transport.configProperties.NotificationProperties;
import com.astrotech.transport.repositories.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CodeGenerator {

    private final TripRepository tripRepository;
    private final NotificationProperties notificationProperties;


    private static final String ALPHANUMERIC_CHARACTERS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int DEFAULT_CODE_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();


    public String generateUniqueTripCode() {
        var prefix = notificationProperties.uniqueName();
        return generateCode(prefix);
    }

    public String generateCode(String prefix) {
        var cleanPrefix = (prefix != null && !prefix.isBlank())
                ? prefix.trim().toUpperCase()
                : "ORANGE";

        String tripCode;
        int maxAttempts = 10;
        int attempts = 0;

        do {
            if (attempts++ >= maxAttempts) {
                throw new IllegalStateException("Failed to generate a unique trip code after " + maxAttempts + " attempts.");
            }
            tripCode = cleanPrefix + "-" + generateRandomAlphanumeric();
        } while (tripRepository.existsByTripCode(tripCode));

        return tripCode;
    }

    private String generateRandomAlphanumeric() {
        StringBuilder sb = new StringBuilder(CodeGenerator.DEFAULT_CODE_LENGTH);
        for (int i = 0; i < CodeGenerator.DEFAULT_CODE_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(ALPHANUMERIC_CHARACTERS.length());
            sb.append(ALPHANUMERIC_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }
}
