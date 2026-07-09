package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetGlucoseRemindersUseCase;
import com.diabecare.application.port.out.LoadGlucoseReminderPort;
import com.diabecare.domain.model.GlucoseReminder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetGlucoseRemindersUseCaseImpl implements GetGlucoseRemindersUseCase {

    private final LoadGlucoseReminderPort loadGlucoseReminderPort;

    @Override
    public List<GlucoseReminder> execute(UUID patientId) {
        return loadGlucoseReminderPort.findByPatientId(patientId);
    }
}
