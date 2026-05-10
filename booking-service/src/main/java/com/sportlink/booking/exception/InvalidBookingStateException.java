package com.sportlink.booking.exception;

/**
 * Thrown when a booking state transition is not allowed (e.g., confirming
 * an already-cancelled booking). Maps to HTTP 409 Conflict.
 */
public class InvalidBookingStateException extends RuntimeException {
    public InvalidBookingStateException(String message) {
        super(message);
    }
}
