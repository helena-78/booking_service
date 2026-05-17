package com.sportlink.search.exception;

/**
 * Thrown when a search or index request is malformed (missing required
 * fields, invalid entity type, etc.). Maps to HTTP 400 Bad Request.
 */
public class InvalidSearchRequestException extends RuntimeException {
    public InvalidSearchRequestException(String message) {
        super(message);
    }
}
