package com.astrotech.transport.core;


import com.astrotech.transport.configProperties.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GenerateVehicleRegistrationNumber {

    private final NotificationProperties notificationProperties;


    public String getNextId(String letter, int nextNumber) {

        char insignia = firstLetter(letter);

        return "%s-%c%03d".formatted(
                notificationProperties.uniqueName(),
                insignia,
                nextNumber
        );
    }

    private char firstLetter(String letter) {
        if (letter == null || letter.isBlank()) {
            throw new IllegalArgumentException("Letter cannot be null or blank");
        }

        return Character.toUpperCase(letter.charAt(0));
    }

    public int extractNumber(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            return 0;
        }

        int lastDashIndex = registrationNumber.lastIndexOf('-');

        if (lastDashIndex == -1 || lastDashIndex == registrationNumber.length() - 1) {
            throw new IllegalArgumentException(
                    "Invalid registration number: " + registrationNumber
            );
        }

        String numberPart = registrationNumber.substring(lastDashIndex + 1);

        try {
            return Integer.parseInt(numberPart);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Invalid registration number: " + registrationNumber,
                    ex
            );
        }
    }
}
