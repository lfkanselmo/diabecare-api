package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SyncMealEntriesUseCase;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.domain.model.MealEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SyncMealEntriesUseCaseImpl implements SyncMealEntriesUseCase {

    private static final LocalDateTime BEGINNING_OF_TIME = LocalDateTime.of(1970, 1, 1, 0, 0);

    private final LoadMealEntryPort loadMealEntryPort;

    @Override
    public List<MealEntry> execute(UUID patientId, LocalDateTime since) {
        return loadMealEntryPort.findByPatientIdUpdatedAfter(
                patientId, since != null ? since : BEGINNING_OF_TIME);
    }
}
