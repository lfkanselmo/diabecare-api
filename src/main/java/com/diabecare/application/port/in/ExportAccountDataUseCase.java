package com.diabecare.application.port.in;

import java.util.UUID;

public interface ExportAccountDataUseCase {

    /**
     * Exporta absolutamente todos los datos personales y de salud de la
     * cuenta como un único archivo JSON (derecho de acceso/portabilidad,
     * Ley 1581 de 2012).
     */
    byte[] exportAsJson(UUID userId);
}
