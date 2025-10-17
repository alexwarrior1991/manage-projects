package com.alejandro.manageprojects.web.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final Map<Class<? extends Throwable>, BiFunction<Throwable, HttpServletRequest, ResponseEntity<ApiError>>> registry = new HashMap<>();

    public GlobalExceptionHandler() {
        // Not Found
        registry.put(NotFoundException.class, (ex, req) -> build(HttpStatus.NOT_FOUND, ErrorType.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req, null));
        // Bad request family
        registry.put(BadRequestException.class, (ex, req) -> build(HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), req, null));
        registry.put(IllegalArgumentException.class, (ex, req) -> build(HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST, "ILLEGAL_ARGUMENT", ex.getMessage(), req, null));
        registry.put(HttpMessageNotReadableException.class, (ex, req) -> build(HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST, "MESSAGE_NOT_READABLE", ex.getMessage(), req, null));
        registry.put(MissingServletRequestParameterException.class, (ex, req) -> build(HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST, "MISSING_PARAMETER", ex.getMessage(), req, null));
        registry.put(MethodArgumentTypeMismatchException.class, (ex, req) -> build(HttpStatus.BAD_REQUEST, ErrorType.BAD_REQUEST, "TYPE_MISMATCH", ex.getMessage(), req, null));
        // Conflict / Data integrity
        registry.put(ConflictException.class, (ex, req) -> build(HttpStatus.CONFLICT, ErrorType.CONFLICT, "CONFLICT", ex.getMessage(), req, null));
        registry.put(DataIntegrityViolationException.class, (ex, req) -> build(HttpStatus.CONFLICT, ErrorType.DATA_INTEGRITY, "DATA_INTEGRITY_VIOLATION", "Violación de integridad de datos", req, null));
        // Service unavailable / infrastructure
        registry.put(ServiceUnavailableException.class, (ex, req) -> build(HttpStatus.SERVICE_UNAVAILABLE, ErrorType.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", ex.getMessage(), req, null));
        registry.put(RedisConnectionFailureException.class, (ex, req) -> build(HttpStatus.SERVICE_UNAVAILABLE, ErrorType.REDIS_UNAVAILABLE, "REDIS_UNAVAILABLE", "Fallo de conexión con Redis", req, null));
        registry.put(TimeoutException.class, (ex, req) -> build(HttpStatus.GATEWAY_TIMEOUT, ErrorType.TIMEOUT, "TIMEOUT", ex.getMessage(), req, null));
        registry.put(ExternalServiceException.class, (ex, req) -> build(HttpStatus.BAD_GATEWAY, ErrorType.EXTERNAL_SERVICE, "EXTERNAL_SERVICE", ex.getMessage(), req, null));
    }

    // Bean Validation - campos de request body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();
        Map<String, Object> details = new HashMap<>();
        details.put("fieldErrors", errors);
        log.info("[ExceptionHandler] Error de validación: {} errores en {}", errors.size(), request != null ? request.getRequestURI() : "-");
        return build(HttpStatus.BAD_REQUEST, ErrorType.VALIDATION, "VALIDATION_ERROR", "Error de validación", request, details);
    }

    // Bean Validation - @RequestParam/@PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ValidationError> errors = ex.getConstraintViolations().stream()
                .map(cv -> new ValidationError(cv.getPropertyPath().toString(), cv.getMessage()))
                .toList();
        Map<String, Object> details = new HashMap<>();
        details.put("violations", errors);
        log.info("[ExceptionHandler] Violaciones de restricción: {} en {}", errors.size(), request != null ? request.getRequestURI() : "-");
        return build(HttpStatus.BAD_REQUEST, ErrorType.VALIDATION, "CONSTRAINT_VIOLATION", "Violaciones de restricción", request, details);
    }

    // Delegación genérica usando registro de lambdas
    @ExceptionHandler({
            NotFoundException.class,
            BadRequestException.class,
            IllegalArgumentException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            ConflictException.class,
            DataIntegrityViolationException.class,
            ServiceUnavailableException.class,
            RedisConnectionFailureException.class,
            TimeoutException.class,
            ExternalServiceException.class
    })
    public ResponseEntity<ApiError> mappedExceptions(Exception ex, HttpServletRequest request) {
        return registry.getOrDefault(ex.getClass(), (e, req) -> build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL, "UNMAPPED", e.getMessage(), req, null))
                .apply(ex, request);
    }

    // Fallback genérico
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("[ExceptionHandler] Error inesperado en {}: {}", request != null ? request.getRequestURI() : "-", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorType.INTERNAL, "INTERNAL_ERROR", "Error inesperado", request, Map.of("exception", ex.getClass().getSimpleName()));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, ErrorType type, String code, String message, HttpServletRequest request, Map<String, Object> details) {
        String path = request != null ? request.getRequestURI() : null;
        ApiError body = ApiError.of(path, status.value(), type, code, message, details);
        if (status.is5xxServerError()) {
            log.warn("[ExceptionHandler] {} {} - {}: {}", status.value(), type, code, message);
        } else {
            log.debug("[ExceptionHandler] {} {} - {}: {}", status.value(), type, code, message);
        }
        return ResponseEntity.status(status).body(body);
    }
}
