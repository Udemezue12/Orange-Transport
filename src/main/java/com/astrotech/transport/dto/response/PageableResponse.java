package com.astrotech.transport.dto.response;

import java.util.List;

public record PageableResponse<T>(
        List<T> content,
        int page,
        int size,
        boolean hasNext,
        boolean hasPrevious
) {}