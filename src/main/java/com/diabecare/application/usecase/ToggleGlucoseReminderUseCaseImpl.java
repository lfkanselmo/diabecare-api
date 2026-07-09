package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ToggleGlucoseReminderUseCase;
import com.diabecare.application.port.out.LoadGlucoseReminderPort;
import com.diabecare.application.port.out.SaveGlucoseReminderPort;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.domain.model.GlucoseReminder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ToggleGlucoseReminderUseCaseImpl implements ToggleGlucoseReminderUseCase {

    private final LoadGlucoseReminderPort loadGlucoseReminderPort;
    private final SaveGlucoseReminderPort saveGlucoseReminderPort;

    @Override
    public GlucoseReminder execute(UUID patientId, UUID reminderId, boolean enabled) {
        var reminder = loadGlucoseReminderPort.findById(reminderId)
                .orElseThrow(() -> new UnauthorizedResourceAccessException(
                        "No tienes permiso para acceder a este recurso"));

        if (!reminder.getPatientId().equals(patientId)) {
            throw new UnauthorizedResourceAccessException("No tienes permiso para acceder a este recurso");
        }

        GlucoseReminder updated = GlucoseReminder.builder()
                .id(reminder.getId())
                .patientId(reminder.getPatientId())
                .reminderTime(reminder.getReminderTime())
                .label(reminder.getLabel())
                .enabled(enabled)
                .createdAt(reminder.getCreatedAt())
                .build();

        return saveGlucoseReminderPort.save(updated);
    }
}
