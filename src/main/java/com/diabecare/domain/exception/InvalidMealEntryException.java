package com.diabecare.domain.exception;

public class InvalidMealEntryException extends DomainException {
    public InvalidMealEntryException(String message) {
        super(message);
    }
}