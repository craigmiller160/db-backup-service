package io.craigmiller160.db.backup.exception;

public class ConfigReadException extends Exception {
    public ConfigReadException(final String message, final Throwable ex) {
        super (message, ex);
    }
}
