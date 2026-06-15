package com.axiserp.catalog.infrastructure.adapters.in.web.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.axiserp.catalog.domain.exception.CategoryHasProductsException;
import com.axiserp.catalog.domain.exception.CategoryNotFoundException;
import com.axiserp.catalog.domain.exception.DuplicateCategoryException;
import com.axiserp.catalog.domain.exception.DuplicateCodigoException;
import com.axiserp.catalog.domain.exception.InvalidPriceException;
import com.axiserp.catalog.domain.exception.ProductNotFoundException;
import com.axiserp.catalog.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.catalog.infrastructure.adapters.in.web.dto.ApiResponse.ApiError;
import com.fasterxml.jackson.core.JsonParseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        List<ApiError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> ApiError.builder()
                        .field(e.getField())
                        .message(e.getDefaultMessage())
                        .rejectedValue(e.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "Error de validación en los datos enviados", errors));
    }

    @ExceptionHandler({DuplicateCodigoException.class, DuplicateCategoryException.class, CategoryHasProductsException.class})
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler({ProductNotFoundException.class, CategoryNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler({InvalidPriceException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "El parametro '" + ex.getName() + "' tiene un formato invalido"));
    }

    @ExceptionHandler(JsonParseException.class)
    public ResponseEntity<ApiResponse<Void>> handleJsonParse(JsonParseException ex) {
        log.warn("json_parse_error: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("BAD_REQUEST", "Formato JSON inválido"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", "No tiene permisos para realizar esta operación"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "Error interno del servidor"));
    }
}
