package com.sportlink.activitymanagement.exception;

/*
 Thrown when a user tries to join an activity that has reached maxParticipants.
 Mapped to HTTP 409 (Conflict) by GlobalExceptionHandler.
 */
public class ActivityFullException extends RuntimeException {
    public ActivityFullException(String message) {
        super(message);
    }
}