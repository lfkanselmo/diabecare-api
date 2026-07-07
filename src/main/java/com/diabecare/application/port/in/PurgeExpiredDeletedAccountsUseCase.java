package com.diabecare.application.port.in;

public interface PurgeExpiredDeletedAccountsUseCase {

    /**
     * Purga en firme las cuentas cuya eliminación (soft-delete) ocurrió hace
     * más del período de gracia. Retorna cuántas cuentas se purgaron.
     */
    int execute();
}
