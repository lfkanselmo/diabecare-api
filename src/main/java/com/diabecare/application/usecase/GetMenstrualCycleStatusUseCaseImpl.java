package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetMenstrualCycleStatusUseCase;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.CyclePhase;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalDouble;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMenstrualCycleStatusUseCaseImpl implements GetMenstrualCycleStatusUseCase {

    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final LoadPatientPort loadPatientPort;

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
        CyclePhase phase = latest.calculateCurrentPhase(today);
        int dayOfCycle = (int) ChronoUnit.DAYS.between(latest.getCycleStartDate(), today) + 1;
        LocalDate nextCycle = latest.predictNextCycleStart();
        String guidance = latest.getPhaseGlucoseGuidance();

        double avgLength = computeAverageLength(history);

        return new CycleStatus(phase, dayOfCycle, nextCycle, guidance, avgLength, history);
    }

    private double computeAverageLength(List<MenstrualCycle> history) {
        if (history.size() < 2) return 28.0;
        OptionalDouble avg = java.util.stream.IntStream.range(0, history.size() - 1)
                .mapToDouble(i -> ChronoUnit.DAYS.between(
                        history.get(i + 1).getCycleStartDate(),
                        history.get(i).getCycleStartDate()))
                .filter(d -> d >= 21 && d <= 35)
                .average();
        return avg.orElse(28.0);
    }
}