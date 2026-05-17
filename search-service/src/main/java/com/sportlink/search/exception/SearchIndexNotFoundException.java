package com.sportlink.search.exception;

/**
 * Thrown when a SearchIndex entry cannot be found by its identity.
 * Maps to HTTP 404.
 */
public class SearchIndexNotFoundException extends RuntimeException {
    public SearchIndexNotFoundException(String message) {
        super(message);
    }
}
