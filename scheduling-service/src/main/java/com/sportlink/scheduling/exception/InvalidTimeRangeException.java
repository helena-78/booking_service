package com.sportlink.scheduling.exception;

/**
 * Thrown when a supplied time range is invalid, e.g. endTime before
 * startTime, or zero-duration range.
 * Mapped to HTTP 400 (Bad Request) by GlobalExceptionHandler.
 */
public class InvalidTimeRangeException extends RuntimeException {
    public InvalidTimeRangeException(String message) {
        super(message);
    }
}