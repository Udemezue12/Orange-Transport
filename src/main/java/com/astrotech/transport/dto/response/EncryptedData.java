package com.astrotech.transport.dto.response;

public record EncryptedData(
        String cipherText,
        String iv) {
}
