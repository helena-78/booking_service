package com.sportlink.moderation.exception;

public class AccountSyncException extends RuntimeException {

    public AccountSyncException(String message) {
        super(message);
    }

    public AccountSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
