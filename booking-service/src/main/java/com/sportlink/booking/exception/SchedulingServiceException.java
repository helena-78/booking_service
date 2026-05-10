package com.sportlink.booking.exception;

/**
 * Thrown when a synchronous call to the Scheduling Service fails or returns
 * a non-success response. Maps to HTTP 503 Service Unavailable via the
 * GlobalExceptionHandler.
 */
public class SchedulingServiceException extends RuntimeException {
    public SchedulingServiceException(String message) {
        super(message);
    }

    public SchedulingServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
