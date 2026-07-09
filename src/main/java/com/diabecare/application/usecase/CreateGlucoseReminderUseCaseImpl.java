package com.diabecare.application.usecase;

import com.diabecare.application.port.in.CreateGlucoseReminderUseCase;
import com.diabecare.application.port.out.SaveGlucoseReminderPort;
import com.diabecare.domain.model.GlucoseReminder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateGlucoseReminderUseCaseImpl implements CreateGlucoseReminderUseCase {

    private final SaveGlucoseReminderPort saveGlucoseReminderPort;

    @Override
    public GlucoseReminder execute(Command command) {
        GlucoseReminder reminder = GlucoseReminder.builder()
                .patientId(command.patientId())
                .reminderTime(command.reminderTime())
                .label(command.label())
                .enabled(true)
                .build();

        return saveGlucoseReminderPort.save(reminder);
    }
}
