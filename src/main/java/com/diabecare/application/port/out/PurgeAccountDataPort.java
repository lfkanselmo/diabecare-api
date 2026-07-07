package com.diabecare.application.port.out;

import java.util.UUID;

public interface PurgeAccountDataPort {

    /**
     * Elimina de forma permanente e irreversible todos los datos clínicos y
     * personales asociados a una cuenta (Ley 1581 de 2012 — derecho de
     * cancelación). {@code patientId} puede ser {@code null} si el usuario
     * nunca llegó a tener un perfil de paciente asociado.
     */
    void purgeAllDataFor(UUID userId, UUID patientId);
}
