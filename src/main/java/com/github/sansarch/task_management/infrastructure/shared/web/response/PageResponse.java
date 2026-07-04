package com.github.sansarch.task_management.infrastructure.shared.web.response;

import com.github.sansarch.task_management.application.shared.dto.PageResult;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <S, T> PageResponse<T> from(PageResult<S> pageResult, Function<S, T> mapper) {
        return new PageResponse<>(
                pageResult.content().stream().map(mapper).toList(),
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
