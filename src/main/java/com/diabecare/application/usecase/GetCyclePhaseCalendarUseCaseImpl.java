package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetCyclePhaseCalendarUseCase;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.model.MenstrualCycle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCyclePhaseCalendarUseCaseImpl implements GetCyclePhaseCalendarUseCase {

    private final LoadMenstrualCyclePort loadMenstrualCyclePort;

    @Override
    public List<DayPhase> getCalendar(UUID patientId, LocalDate from, LocalDate to) {
        MenstrualCycle latestCycle = loadMenstrualCyclePort.findLatestByPatientId(patientId)
                .orElseThrow(() -> new InvalidPatientDataException(
                        "No hay ciclos registrados. Registra tu primer ciclo."));

        return from.datesUntil(to.plusDays(1))
                .map(date -> new DayPhase(date, latestCycle.calculateCurrentPhase(date)))
                .toList();
    }
}