package com.sportlink.moderation.exception;

public class SanctionNotFoundException extends RuntimeException {

    public SanctionNotFoundException(String message) {
        super(message);
    }
}
