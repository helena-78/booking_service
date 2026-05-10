package com.sportlink.scheduling.exception;

/**
 * Thrown when a time slot operation conflicts with the slot's current state
 * (e.g. trying to reserve a slot that is already RESERVED, or release a
 * slot that has already been RELEASED).
 * Mapped to HTTP 409 (Conflict) by GlobalExceptionHandler.
 */
public class TimeSlotConflictException extends RuntimeException {
    public TimeSlotConflictException(String message) {
        super(message);
    }
}