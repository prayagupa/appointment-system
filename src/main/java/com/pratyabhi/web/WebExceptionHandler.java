package com.pratyabhi.web;

import com.pratyabhi.exception.AppointmentNotFoundException;
import com.pratyabhi.exception.DomainException;
import com.pratyabhi.exception.InvalidArgumentException;
import com.pratyabhi.exception.ProviderNotFoundException;
import com.pratyabhi.exception.SlotUnavailableException;
import com.pratyabhi.exception.TenantNotFoundException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = AppointmentApiController.class)
public class WebExceptionHandler {

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(TenantNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({ProviderNotFoundException.class, AppointmentNotFoundException.class})
    public ResponseEntity<Map<String, String>> entityNotFound(DomainException ex) {
        return error(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(SlotUnavailableException.class)
    public ResponseEntity<Map<String, String>> slotUnavailable(SlotUnavailableException ex) {
        return error(HttpStatus.CONFLICT, ex);
    }

    @ExceptionHandler({InvalidArgumentException.class, IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> badRequest(RuntimeException ex) {
        return error(HttpStatus.BAD_REQUEST, ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException ex) {
        String message =
                ex.getBindingResult().getFieldErrors().stream()
                        .findFirst()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .orElse("Validation failed");
        return ResponseEntity.badRequest().body(Map.of("code", "INVALID_ARGUMENT", "message", message));
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, DomainException ex) {
        return ResponseEntity.status(status)
                .body(Map.of("code", ex.getErrorCode(), "message", ex.getMessage()));
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, RuntimeException ex) {
        return ResponseEntity.status(status)
                .body(Map.of("code", "INVALID_ARGUMENT", "message", ex.getMessage()));
    }
}
