package com.diabecare.domain.exception;

public class InvalidPatientDataException extends DomainException {
    public InvalidPatientDataException(String message) {
        super(message);
    }
}