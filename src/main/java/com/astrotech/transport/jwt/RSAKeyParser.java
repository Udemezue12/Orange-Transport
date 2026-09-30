package com.astrotech.transport.jwt;

import org.jspecify.annotations.NonNull;

import java.security.KeyFactory;
import java.security.interfaces.*;
import java.security.spec.*;
import java.util.Base64;

public class RSAKeyParser {
    public static RSAPrivateKey parsePrivateKey(String pem) throws Exception {

        byte[] encoded = RSAKeyParser.decodePem(
                pem,
                "PRIVATE KEY"
        );

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(encoded)
        );
    }
    public static RSAPublicKey parsePublicKey(String pem) throws Exception {

        byte[] encoded = decodePem(
                pem,
                "PUBLIC KEY"
        );

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(encoded)
        );
    }

    public static byte[] decodePem(String pem, String type) {

        if (pem == null || pem.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT " + type + " key is missing"
            );
        }

        String normalized = pem
                .trim()

                .replace("\\n", "\n")

                .replace("\r", "");

        var base64 = getString(type, normalized);

        try {
            return Base64.getDecoder().decode(base64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid Base64 content in " + type + " PEM key",
                    e
            );
        }
    }

    private static @NonNull String getString(String type, String normalized) {
        String beginMarker = "-----BEGIN " + type + "-----";
        String endMarker = "-----END " + type + "-----";

        if (!normalized.contains(beginMarker)) {
            throw new IllegalArgumentException(
                    "Invalid PEM: missing " + beginMarker
            );
        }

        if (!normalized.contains(endMarker)) {
            throw new IllegalArgumentException(
                    "Invalid PEM: missing " + endMarker
            );
        }

        return normalized
                .replace(beginMarker, "")
                .replace(endMarker, "")
                .replaceAll("\\s+", "");
    }

    public static void validateKeyPair(
            RSAPrivateKey privateKey,
            RSAPublicKey publicKey,
            String purpose
    ) {
        if (!privateKey.getModulus().equals(publicKey.getModulus())) {
            throw new IllegalStateException(
                    "JWT " + purpose +
                            " private key does not match public key"
            );
        }


    }

}
