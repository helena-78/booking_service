package com.sportlink.scheduling.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates exceptions into clean JSON error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TimeSlotNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(TimeSlotNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TimeSlotConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(TimeSlotConflictException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidTimeRangeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRange(InvalidTimeRangeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Catches "I cannot do this in the current state" type errors thrown by
     * the service. HTTP 409 (Conflict) fits the semantics.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Triggered by @Valid violations on request DTOs. Aggregates all field
     * errors into one message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return build(HttpStatus.BAD_REQUEST, msg);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}