package com.diabecare.application.port.in;

import java.util.UUID;

public interface ChangeUserRoleUseCase {

    record Command(UUID userId, String newRole) {}

    void execute(Command command);
}
