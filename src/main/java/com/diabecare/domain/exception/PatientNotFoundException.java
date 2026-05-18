package com.diabecare.domain.exception;

public class PatientNotFoundException extends DomainException {
    public PatientNotFoundException(String id) {
        super("Paciente no encontrado con id: " + id);
    }
}