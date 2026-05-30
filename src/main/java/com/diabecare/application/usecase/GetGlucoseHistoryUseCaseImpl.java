package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetGlucoseHistoryUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.domain.model.GlucoseReading;
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
public class GetGlucoseHistoryUseCaseImpl implements GetGlucoseHistoryUseCase {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final LoadMealEntryPort loadMealEntryPort;

    @Override
    public Result getByPatientAndDateRange(UUID patientId,
                                           LocalDateTime from,
                                           LocalDateTime to) {
        List<GlucoseReading> readings = loadGlucoseReadingPort
                .findByPatientIdAndDateRange(patientId, from, to);

        List<MealEntry> meals = loadMealEntryPort
                .findByPatientIdAndDateRange(patientId, from.toLocalDate(), to.toLocalDate());

        return new Result(readings, meals);
    }
}