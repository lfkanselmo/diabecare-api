package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetMenstrualCycleStatusUseCase;
import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadCycleDayEntryPort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CyclePhase;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.CycleStatisticsService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMenstrualCycleStatusUseCaseImpl implements GetMenstrualCycleStatusUseCase {

    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final LoadCycleDayEntryPort loadCycleDayEntryPort;
    private final LoadPatientPort loadPatientPort;
    private final MenstrualCycleGuidanceService cycleGuidanceService;
    private final CycleStatisticsService cycleStatisticsService;
    private final AlertConfigPort alertConfig;

    @Override
    public CycleStatus getStatus(UUID patientId) {
        Patient patient = loadPatientPort.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException(patientId.toString()));

        if (!patient.isFemale()) {
            throw new InvalidPatientDataException(
                    "Esta funcionalidad solo está disponible para pacientes de sexo femenino.");
        }

        List<MenstrualCycle> history = loadMenstrualCyclePort.findByPatientId(patientId);

        MenstrualCycle latest = loadMenstrualCyclePort.findLatestByPatientId(patientId)
                .orElseThrow(() -> new InvalidPatientDataException(
                        "No hay ciclos registrados. Registra tu primer ciclo."));

        LocalDate today = LocalDate.now();

        Integer avgCycleLength = cycleStatisticsService.calculateAverageCycleLength(history);
        Integer avgPeriodLength = cycleStatisticsService.calculateAveragePeriodLength(history);

        CyclePhase phase = latest.calculateCurrentPhase(today, avgCycleLength, avgPeriodLength);
        int dayOfCycle = latest.calculateDayOfCycle(today, avgCycleLength);
        LocalDate nextCycle = latest.predictNextCycleStart(today, avgCycleLength);
        boolean isProjectionStale = latest.isProjectionStale(today, avgCycleLength);
        String guidance = cycleGuidanceService.resolveGuidance(phase);

        boolean isOpenTooLong = latest.isOngoing() &&
                ChronoUnit.DAYS.between(latest.getStartDate(), today) >= alertConfig.daysBeforeOpenCycleAlert();

        CycleDayEntry todayEntry = loadCycleDayEntryPort
                .findByCycleIdAndDate(latest.getCycleId(), today)
                .orElse(null);

        return new CycleStatus(
                phase,
                dayOfCycle,
                latest.isOngoing(),
                isOpenTooLong,
                isProjectionStale,
                latest.getStartDate(),
                nextCycle,
                guidance,
                avgCycleLength,
                avgPeriodLength,
                todayEntry,
                history
        );
    }
}