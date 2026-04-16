package com.sporty.feed.domain.model;

/**
 * Thrown when a provider sends an outcome value that cannot be mapped
 * to a known {@link Outcome}. Extends {@link IllegalArgumentException} so
 * callers that already catch the broader type are unaffected, but the
 * exception handler can be narrowed to this specific type to avoid
 * accidentally swallowing unrelated framework exceptions.
 */
public class UnknownOutcomeException extends IllegalArgumentException {

    public UnknownOutcomeException(String message) {
        super(message);
    }
}
