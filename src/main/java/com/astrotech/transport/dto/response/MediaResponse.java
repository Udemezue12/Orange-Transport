package com.astrotech.transport.dto.response;

import com.astrotech.transport.enums.MediaType;

public record MediaResponse(
        String publicId,
        String secureUrl,
        String originalName,
        MediaType mediaType,
        Integer duration,
        Long fileSize

) {
}
