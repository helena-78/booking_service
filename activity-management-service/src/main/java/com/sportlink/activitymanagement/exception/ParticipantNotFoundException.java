package com.sportlink.activitymanagement.exception;

/*
  Thrown when an operation references a participant that is not actually
  enrolled in the given activity (e.g. user tries to leave an activity they
  never joined). Mapped to HTTP 404 by GlobalExceptionHandler.
 */
public class ParticipantNotFoundException extends RuntimeException {
    public ParticipantNotFoundException(String message) {
        super(message);
    }
}