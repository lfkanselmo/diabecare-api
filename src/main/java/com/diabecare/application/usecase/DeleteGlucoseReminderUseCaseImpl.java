package com.diabecare.application.usecase;

import com.diabecare.application.port.in.DeleteGlucoseReminderUseCase;
import com.diabecare.application.port.out.LoadGlucoseReminderPort;
import com.diabecare.application.port.out.SaveGlucoseReminderPort;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class DeleteGlucoseReminderUseCaseImpl implements DeleteGlucoseReminderUseCase {

    private final LoadGlucoseReminderPort loadGlucoseReminderPort;
    private final SaveGlucoseReminderPort saveGlucoseReminderPort;

    @Override
    public void execute(UUID patientId, UUID reminderId) {
        var reminder = loadGlucoseReminderPort.findById(reminderId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException(
                        "No tienes permiso para acceder a este recurso"));

        if (!reminder.getPatientId().equals(patientId)) {
            throw new UnauthorizedResourceAccessException("No tienes permiso para acceder a este recurso");
        }

        saveGlucoseReminderPort.delete(reminderId);
    }
}
