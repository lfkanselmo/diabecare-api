package com.diabecare.domain.exception;

public class InvalidPasswordResetTokenException extends DomainException {
    public InvalidPasswordResetTokenException() {
        super("El enlace de recuperación es inválido, ya fue usado o expiró.");
    }
}
