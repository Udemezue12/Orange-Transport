package com.astrotech.transport.utilities.hash;



import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@RequiredArgsConstructor
public class RequestHashUtil {


    private final ObjectMapper objectMapper;

    public String hash(Object request) {

        try {

            var json =
                    objectMapper.writeValueAsString(request);

            var digest =
                    MessageDigest.getInstance("SHA-256");

            var hash =
                    digest.digest(
                            json.getBytes(StandardCharsets.UTF_8));

            var sb = new StringBuilder();

            for (byte b : hash) {
                sb.append(
                        String.format("%02x", b));
            }

            return sb.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

