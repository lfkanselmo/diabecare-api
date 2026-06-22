package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetMealHistoryUseCase;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.domain.model.MealEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMealHistoryUseCaseImpl implements GetMealHistoryUseCase {

    private final LoadMealEntryPort loadMealEntryPort;

    @Override
    public List<MealEntry> getHistory(UUID patientId, LocalDate from, LocalDate to) {
        return loadMealEntryPort.findByPatientIdAndDateRange(patientId, from, to);
    }
}