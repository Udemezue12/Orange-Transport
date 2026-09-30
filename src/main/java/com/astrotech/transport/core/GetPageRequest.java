package com.astrotech.transport.core;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

public class GetPageRequest {
    public static Pageable getPageableWithSorting(int page, int size, String sortBy, boolean withAscending, Class<?> entityClass, boolean withSorting) {
        var pagination = new GetCalculatedPagination(page, size);
        if (withSorting) {
            return PageRequest.of(pagination.page(), pagination.size(), getSort(sortBy, withAscending, entityClass, "id"));
        } else {
            return PageRequest.of(pagination.page(), pagination.size());

        }


    }


    public static Sort getSort(
            String sortBy,
            boolean withAscending,
            Class<?> entityClass,
            String defaultSortBy) {


        if (sortBy == null || sortBy.isBlank()) {
            sortBy = defaultSortBy;
        } else {

            String finalSortBy = sortBy;
            boolean fieldExists = Arrays.stream(entityClass.getDeclaredFields())
                    .anyMatch(field -> field.getName().equals(finalSortBy));

            if (!fieldExists) {
                sortBy = defaultSortBy;
            }
        }


        return withAscending ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    }
}
