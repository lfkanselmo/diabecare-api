package com.diabecare.application.port.in;

import java.util.UUID;

public interface RevokeCaregiverAccessUseCase {

    record Command(UUID patientId, UUID linkId) {}

    void execute(Command command);
}
