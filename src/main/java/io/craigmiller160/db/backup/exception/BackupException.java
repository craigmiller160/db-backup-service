package io.craigmiller160.db.backup.exception;

public class BackupException extends Exception {
    public BackupException(final String message) {
        super (message);
    }

    public BackupException(final String message, final Throwable ex) {
        super (message, ex);
    }
}
