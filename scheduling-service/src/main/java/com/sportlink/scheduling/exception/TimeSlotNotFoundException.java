package com.sportlink.scheduling.exception;

/**
 * Thrown when a time slot is requested by ID but not found in the database.
 * Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class TimeSlotNotFoundException extends RuntimeException {
    public TimeSlotNotFoundException(String message) {
        super(message);
    }
}