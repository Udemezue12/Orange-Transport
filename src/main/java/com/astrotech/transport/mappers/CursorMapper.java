package com.astrotech.transport.mappers;

import com.astrotech.transport.dto.response.CursorResponse;
import org.springframework.data.redis.core.Cursor;

import java.util.List;

public class CursorMapper {

    public static <T> CursorResponse<T> getResponse(List<T> results, String nextCursor, boolean hasNextPage) {
        return new CursorResponse<>(results, nextCursor, hasNextPage);
    }
}

