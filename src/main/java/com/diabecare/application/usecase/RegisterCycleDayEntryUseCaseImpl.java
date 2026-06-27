package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RegisterCycleDayEntryUseCase;
import com.diabecare.application.port.out.LoadCycleDayEntryPort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.SaveCycleDayEntryPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CycleSymptomEntry;
import com.diabecare.domain.model.MenstrualCycle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterCycleDayEntryUseCaseImpl implements RegisterCycleDayEntryUseCase {

    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final LoadCycleDayEntryPort loadCycleDayEntryPort;
    private final SaveCycleDayEntryPort saveCycleDayEntryPort;

    @Override
    public CycleDayEntry execute(Command command) {
        MenstrualCycle cycle = loadMenstrualCyclePort.findLatestByPatientId(command.patientId())
                .filter(MenstrualCycle::isOngoing)
                .orElseThrow(() -> new InvalidPatientDataException(
                        "No tienes un ciclo en curso. Registra el inicio de tu período primero."));

        if (command.entryDate().isBefore(cycle.getStartDate())) {
            throw new InvalidPatientDataException(
                    "La fecha del registro no puede ser anterior al inicio del ciclo actual.");
        }

        var symptoms = command.symptoms() == null ? java.util.List.<CycleSymptomEntry>of() :
                command.symptoms().stream()
                        .map(s -> CycleSymptomEntry.builder()
                                .symptom(s.symptom())
                                .severity(s.severity())
                                .build())
                        .toList();

        CycleDayEntry existing = loadCycleDayEntryPort
                .findByCycleIdAndDate(cycle.getCycleId(), command.entryDate())
                .orElse(null);

        CycleDayEntry entry = CycleDayEntry.builder()
                .dayEntryId(existing != null ? existing.getDayEntryId() : null)
                .cycleId(cycle.getCycleId())
                .patientId(command.patientId())
                .entryDate(command.entryDate())
                .flowIntensity(command.flowIntensity())
                .notes(command.notes())
                .symptoms(symptoms)
                .build();

        return saveCycleDayEntryPort.save(entry);
    }
}