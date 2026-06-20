package com.diabecare.domain.exception;

public class InvalidRefreshTokenException extends DomainException {
    public InvalidRefreshTokenException() {
        super("El token de actualización es inválido, ya fue usado o expiró.");
    }
}