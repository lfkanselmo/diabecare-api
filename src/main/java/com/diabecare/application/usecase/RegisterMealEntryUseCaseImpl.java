package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMealEntryUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveMealEntryPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterMealEntryUseCaseImpl implements RegisterMealEntryUseCase {

    private final SaveMealEntryPort  saveMealEntryPort;
    private final LoadPatientPort    loadPatientPort;
    private final RateLimitService   rateLimitService;

    @Override
    public MealEntry execute(Command command) {
        rateLimitService.checkMealLimit(command.patientId());

        loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        MealEntry entry = MealEntry.create(
                command.patientId(),
                command.mealType(),
                command.consumedAt(),
                command.notes()
        );
        command.items().forEach(entry::addItem);
        return saveMealEntryPort.save(entry);
    }
}