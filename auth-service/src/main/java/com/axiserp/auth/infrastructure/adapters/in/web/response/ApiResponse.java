package com.axiserp.auth.infrastructure.adapters.in.web.response;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private String timestamp;
    private ErrorDetail error;
    private Pagination pagination;

    public ApiResponse() {
        this.timestamp = Instant.now().toString();
    }

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = "Operación exitosa";
        return response;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }

    public static <T> ApiResponse<T> created(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = "Recurso creado exitosamente";
        return response;
    }

    public static <T> ApiResponse<T> error(int status, String error, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorDetail(status, error, message);
        response.message = message;
        return response;
    }

    public static <T> ApiResponse<T> error(int status, String error, String message, Map<String, String> details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = new ErrorDetail(status, error, message, details);
        response.message = message;
        return response;
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }

    public boolean isSuccess() { return success; }
    public T getData() { return data; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public ErrorDetail getError() { return error; }
    public Pagination getPagination() { return pagination; }

    public void setPagination(int page, int size, long totalElements, int totalPages) {
        this.pagination = new Pagination(page, size, totalElements, totalPages);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetail {
        private int status;
        private String error;
        private String message;
        private Map<String, String> details;

        ErrorDetail() {}

        ErrorDetail(int status, String error, String message) {
            this.status = status;
            this.error = error;
            this.message = message;
        }

        ErrorDetail(int status, String error, String message, Map<String, String> details) {
            this.status = status;
            this.error = error;
            this.message = message;
            this.details = details;
        }

        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public Map<String, String> getDetails() { return details; }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Pagination {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;

        Pagination() {}

        Pagination(int page, int size, long totalElements, int totalPages) {
            this.page = page;
            this.size = size;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
        }

        public int getPage() { return page; }
        public int getSize() { return size; }
        public long getTotalElements() { return totalElements; }
        public int getTotalPages() { return totalPages; }
    }
}
