package com.diabecare.domain.exception;

import java.time.LocalDate;

public class OpenCycleConflictException extends DomainException {

    private final LocalDate openCycleStartDate;

    public OpenCycleConflictException(LocalDate openCycleStartDate) {
        super("Ya tienes un ciclo en curso iniciado el " + openCycleStartDate +
                ". Indica cuándo terminó tu período antes de registrar uno nuevo.");
        this.openCycleStartDate = openCycleStartDate;
    }

    public LocalDate getOpenCycleStartDate() {
        return openCycleStartDate;
    }
}