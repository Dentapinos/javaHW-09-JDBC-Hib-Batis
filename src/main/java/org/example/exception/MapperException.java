package org.example.exception;

public class MapperException extends RepositoryException {
    public MapperException(String message) {
        super(message);
    }

    public MapperException(String message, Throwable cause) {
        super(message, cause);
    }
}