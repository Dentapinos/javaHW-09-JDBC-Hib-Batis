package org.example.exception;

public class EntityUpdateException extends RepositoryException {
    public EntityUpdateException(String message) {
        super(message);
    }

    public EntityUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
