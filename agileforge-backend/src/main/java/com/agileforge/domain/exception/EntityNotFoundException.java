package com.agileforge.domain.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entity, Object id) {
        super(String.format("%s not found with id: %s", entity, id));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
