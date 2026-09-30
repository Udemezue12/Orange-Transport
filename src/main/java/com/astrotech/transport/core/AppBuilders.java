package com.astrotech.transport.core;

import com.astrotech.transport.dto.request.NameParts;

import java.util.Arrays;

public class AppBuilders {

    public static String joinStrings(String first, String middle, String last) {
        StringBuilder sb = new StringBuilder();


        if (first != null && !first.isBlank()) {
            sb.append(first.trim());
        }


        if (middle != null && !middle.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(middle.trim());
        }


        if (last != null && !last.isBlank()) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(last.trim());
        }

        return sb.toString();
    }
    public static NameParts splitStrings(String fullName) {

            if (fullName == null || fullName.isBlank()) {
                return new NameParts(null, null, null);
            }

            String[] parts = fullName.trim().split("\\s+");

            if (parts.length == 1) {
                return new NameParts(parts[0], null, null);
            }

            if (parts.length == 2) {
                return new NameParts(parts[0], null, parts[1]);
            }

            String middleName = String.join(" ",
                    Arrays.copyOfRange(parts, 1, parts.length - 1));

            return new NameParts(
                    parts[0],
                    middleName,
                    parts[parts.length - 1]
            );
        }

}
