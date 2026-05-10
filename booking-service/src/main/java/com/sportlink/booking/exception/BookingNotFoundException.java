package com.sportlink.booking.exception;

/**
 * Thrown when a booking cannot be found by its ID. Maps to HTTP 404.
 */
public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
