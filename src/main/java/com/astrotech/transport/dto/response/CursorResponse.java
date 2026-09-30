package com.astrotech.transport.dto.response;

import java.util.List;

public record CursorResponse<T>(
        List<T> data,
        String nextCursor,
        boolean hasNextPage
) {
}

