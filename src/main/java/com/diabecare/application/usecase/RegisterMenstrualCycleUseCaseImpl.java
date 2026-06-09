package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterMenstrualCycleUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveMenstrualCyclePort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.OptionalDouble;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterMenstrualCycleUseCaseImpl implements RegisterMenstrualCycleUseCase {

    private final SaveMenstrualCyclePort saveMenstrualCyclePort;
    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final LoadPatientPort loadPatientPort;

    @Override
    public MenstrualCycle execute(Command command) {
        Patient patient = loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        if (!patient.isFemale()) {
            throw new InvalidPatientDataException(
                    "El seguimiento del ciclo menstrual solo está disponible " +
                            "para pacientes de sexo femenino.");
        }

        MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                command.patientId(),
                command.startDate(),
                command.periodLengthDays(),
                command.symptoms(),
                command.notes()
        );

        List<MenstrualCycle> history = loadMenstrualCyclePort
                .findByPatientId(command.patientId());

        if (history.size() >= 2) {
            OptionalDouble avgLength = computeAverageCycleLength(history);
            if (avgLength.isPresent()) {
                cycle = MenstrualCycle.builder()
                        .cycleId(cycle.getCycleId())
                        .patientId(cycle.getPatientId())
                        .cycleStartDate(cycle.getCycleStartDate())
                        .periodLengthDays(cycle.getPeriodLengthDays())
                        .cycleLengthDays((int) Math.round(avgLength.getAsDouble()))
                        .phase(cycle.getPhase())
                        .symptoms(cycle.getSymptoms())
                        .notes(cycle.getNotes())
                        .build();
            }
        }

        return saveMenstrualCyclePort.save(cycle);
    }

    private OptionalDouble computeAverageCycleLength(List<MenstrualCycle> history) {
        if (history.size() < 2) return OptionalDouble.empty();
        return java.util.stream.IntStream.range(0, history.size() - 1)
                .mapToDouble(i -> ChronoUnit.DAYS.between(
                        history.get(i + 1).getCycleStartDate(),
                        history.get(i).getCycleStartDate()))
                .filter(d -> d >= 21 && d <= 35)
                .average();
    }
}