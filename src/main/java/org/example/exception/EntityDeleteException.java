package org.example.exception;

public class EntityDeleteException extends RepositoryException {
    public EntityDeleteException(String message) {
        super(message);
    }

    public EntityDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}