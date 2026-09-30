package com.astrotech.transport.dto.response;

import java.util.List;

public record SliceResponse<T>(
        List<T> content,
        int page,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {}
