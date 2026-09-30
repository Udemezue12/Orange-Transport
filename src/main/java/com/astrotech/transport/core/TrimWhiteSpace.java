package com.astrotech.transport.core;

import com.astrotech.transport.dto.response.TrimmedResult;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TrimWhiteSpace {
    public static String trimWhiteSpace(String str) {

        return str == null ? null : str.trim();
    }

    public static String trimWhiteSpaceWithUpperCase(String str, boolean withUpperCase){
        if (withUpperCase)  {
            return str == null ? null : str.trim().toUpperCase();

        }
        else {
            return str == null ? null : str.trim().toLowerCase();

        }
    }
    public static String trimWhiteSpaceAndCapitalize(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }

        return Arrays.stream(input.strip().split("\\s+"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
    public static TrimmedResult getTrimmedResult(String email, String firstName, String lastName) {
        var trimmedEmail = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(email, false);
        var trimmedFirstName = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(firstName, false);
        var trimmedLastName = TrimWhiteSpace.trimWhiteSpaceWithUpperCase(lastName, false);
        var fullName = AppBuilders.joinStrings(trimmedLastName, null, trimmedFirstName);

        return new TrimmedResult(trimmedEmail, fullName);
    }




    public static String sanitize(String url) {

        return url
                .replace("https://", "")
                .replace("http://", "")
                .replaceAll("[^a-zA-Z0-9]", "-");

    }

}
