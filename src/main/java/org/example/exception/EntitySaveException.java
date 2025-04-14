package org.example.exception;

public class EntitySaveException extends RepositoryException {
    public EntitySaveException(String message) {
        super(message);
    }

    public EntitySaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
