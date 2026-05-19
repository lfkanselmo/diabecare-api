package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetGlucoseHistoryUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
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

    @Override
    public List<GlucoseReading> getByPatientAndDateRange(UUID patientId,
                                                         LocalDateTime from,
                                                         LocalDateTime to) {
        return loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to);
    }
}