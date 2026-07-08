package com.diabecare.domain.exception;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException(String id) {
        super("Usuario no encontrado con id: " + id);
    }
}
