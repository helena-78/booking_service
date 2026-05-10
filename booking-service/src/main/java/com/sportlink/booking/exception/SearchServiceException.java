package com.sportlink.booking.exception;

/**
 * Thrown when a synchronous call to the Search Service fails or returns
 * a non-success response. Maps to HTTP 503 Service Unavailable via the
 * GlobalExceptionHandler.
 */
public class SearchServiceException extends RuntimeException {
    public SearchServiceException(String message) {
        super(message);
    }

    public SearchServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
