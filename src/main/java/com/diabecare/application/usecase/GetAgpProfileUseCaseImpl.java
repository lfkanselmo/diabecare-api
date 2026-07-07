package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetAgpProfileUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.AgpHourlyBucket;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.service.AgpProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAgpProfileUseCaseImpl implements GetAgpProfileUseCase {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final AgpProfileService agpProfileService;

    @Override
    public List<AgpHourlyBucket> execute(UUID patientId, LocalDateTime from, LocalDateTime to) {
        List<GlucoseReading> readings =
                loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to);
        return agpProfileService.buildHourlyProfile(readings);
    }
}
