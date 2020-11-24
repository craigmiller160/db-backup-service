package io.craigmiller160.db.backup.exception;

public class PropertyException extends Exception {
    public PropertyException(final String message) {
        super (message);
    }

    public PropertyException(final String message, final Throwable cause) {
        super (message, cause);
    }
}