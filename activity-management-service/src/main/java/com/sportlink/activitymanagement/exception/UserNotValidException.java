package com.sportlink.activitymanagement.exception;

/**
 * Thrown when Account Management Service reports a user as missing,
 * suspended, or banned, and we cannot proceed with the requested operation.
 * Mapped to HTTP 400 (Bad Request) by GlobalExceptionHandler.
 */
public class UserNotValidException extends RuntimeException {
    public UserNotValidException(String message) {
        super(message);
    }
}