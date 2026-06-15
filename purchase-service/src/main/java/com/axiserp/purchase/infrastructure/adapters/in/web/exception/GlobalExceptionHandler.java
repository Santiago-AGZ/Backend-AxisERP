package com.axiserp.purchase.infrastructure.adapters.in.web.exception;

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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.axiserp.purchase.domain.exception.DuplicateNitException;
import com.axiserp.purchase.domain.exception.DuplicateProductInPurchaseException;
import com.axiserp.purchase.domain.exception.PurchaseNotFoundException;
import com.axiserp.purchase.domain.exception.PurchaseNotModifiableException;
import com.axiserp.purchase.domain.exception.SupplierInactiveException;
import com.axiserp.purchase.domain.exception.SupplierNotFoundException;
import com.axiserp.purchase.infrastructure.adapters.in.web.dto.ApiResponse;
import com.axiserp.purchase.infrastructure.adapters.in.web.dto.ApiResponse.ApiError;
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
                .body(ApiResponse.error("VALIDATION_ERROR", "Error de validacion en los datos enviados", errors));
    }

    @ExceptionHandler({SupplierNotFoundException.class, PurchaseNotFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler({
        SupplierInactiveException.class, DuplicateNitException.class,
        DuplicateProductInPurchaseException.class, PurchaseNotModifiableException.class,
        IllegalStateException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "El parametro '" + ex.getName() + "' tiene un formato invalido"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("VALIDATION_ERROR", "El parametro requerido '" + ex.getParameterName() + "' no fue enviado"));
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
                .body(ApiResponse.error("FORBIDDEN", "No tiene permisos para realizar esta operacion"));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceUnavailable(ResourceAccessException ex) {
        log.error("Servicio externo no disponible: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("SERVICE_UNAVAILABLE", "Servicio externo no disponible: " + ex.getMessage()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpClientError(HttpClientErrorException ex) {
        log.error("Error en llamada externa: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity.status(ex.getStatusCode())
                .body(ApiResponse.error("EXTERNAL_ERROR", "Error en servicio externo: " + ex.getMessage()));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpServerError(HttpServerErrorException ex) {
        log.error("Error en servidor externo: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error("EXTERNAL_SERVER_ERROR", "Error en servidor externo: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        String detail = ex.getMessage() != null ? ex.getMessage() : "Error desconocido";
        if (ex.getCause() != null && ex.getCause().getMessage() != null) {
            detail = ex.getCause().getMessage();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", detail));
    }
}
