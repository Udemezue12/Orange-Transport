package com.astrotech.transport.webhooks;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class WebhookValidator {

    private static final String HMAC_SHA512 = "HmacSHA512";

    public static boolean isValidHmacSha512(String jsonPayload, String requestSignature, String secretKey) {
        if (requestSignature == null || jsonPayload == null || secretKey == null) {
            return false;
        }
        try {
            var sha512Hmac = Mac.getInstance(HMAC_SHA512);
            var secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            sha512Hmac.init(secretKeySpec);

            byte[] macData = sha512Hmac.doFinal(jsonPayload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : macData) {
                hexString.append(String.format("%02x", b));
            }

            return isEqual(hexString.toString(), requestSignature);
        } catch (Exception e) {
            return false;
        }
    }
    public static boolean isValidMonnifySignature(String jsonPayload, String requestSignature, String secretKey) {
        if (requestSignature == null || jsonPayload == null || secretKey == null) {
            return false;
        }
        try {
            Mac sha512Hmac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            sha512Hmac.init(secretKeySpec);

            byte[] macData = sha512Hmac.doFinal(jsonPayload.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : macData) {
                hexString.append(String.format("%02x", b));
            }

            return isEqual(hexString.toString(), requestSignature);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isEqual(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8)
        );
    }
}