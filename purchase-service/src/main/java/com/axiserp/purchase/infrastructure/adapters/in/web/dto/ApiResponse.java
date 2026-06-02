package com.axiserp.purchase.infrastructure.adapters.in.web.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private final List<ApiError> errors;
    private final ApiMeta meta;
    private final PaginationMeta pagination;

    // ── Factories ────────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .data(data)
                .meta(ApiMeta.now())
                .build();
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("CREATED")
                .message(message)
                .data(data)
                .meta(ApiMeta.now())
                .build();
    }

    public static <T> ApiResponse<T> paged(T data, String message, PaginationMeta pagination) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .message(message)
                .data(data)
                .pagination(pagination)
                .meta(ApiMeta.now())
                .build();
    }

    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .meta(ApiMeta.now())
                .build();
    }

    public static ApiResponse<Void> error(String code, String message, List<ApiError> errors) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(code)
                .message(message)
                .errors(errors)
                .meta(ApiMeta.now())
                .build();
    }

    // ── Nested types ─────────────────────────────────────────────────────────

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ApiError {
        private final String field;
        private final String message;
        private final Object rejectedValue;
    }

    @Getter
    @Builder
    public static class ApiMeta {
        private final Instant timestamp;
        private final String requestId;

        public static ApiMeta now() {
            return ApiMeta.builder()
                    .timestamp(Instant.now())
                    .requestId(java.util.UUID.randomUUID().toString())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class PaginationMeta {
        private final int page;
        private final int pageSize;
        private final long totalRecords;
        private final int totalPages;
        private final boolean hasNext;
        private final boolean hasPrevious;

        public static PaginationMeta of(int page, int pageSize, long total) {
            int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
            return PaginationMeta.builder()
                    .page(page)
                    .pageSize(pageSize)
                    .totalRecords(total)
                    .totalPages(totalPages)
                    .hasNext(page < totalPages)
                    .hasPrevious(page > 1)
                    .build();
        }
    }
}

