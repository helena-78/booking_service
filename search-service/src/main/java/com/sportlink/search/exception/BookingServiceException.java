package com.sportlink.search.exception;

/**
 * Thrown when a synchronous call to the Booking Service fails or returns a
 * non-success response. Maps to HTTP 503 Service Unavailable via the
 * GlobalExceptionHandler.
 */
public class BookingServiceException extends RuntimeException {
    public BookingServiceException(String message) {
        super(message);
    }

    public BookingServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
