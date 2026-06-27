package com.diabecare.application.usecase;

import com.diabecare.application.port.in.FinishPeriodUseCase;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.SaveMenstrualCyclePort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.model.MenstrualCycle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FinishPeriodUseCaseImpl implements FinishPeriodUseCase {

    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final SaveMenstrualCyclePort saveMenstrualCyclePort;

    @Override
    public MenstrualCycle execute(Command command) {
        MenstrualCycle cycle = loadMenstrualCyclePort.findLatestByPatientId(command.patientId())
                .filter(MenstrualCycle::isOngoing)
                .orElseThrow(() -> new InvalidPatientDataException(
                        "No tienes un período en curso para finalizar."));

        if (command.endDate().isBefore(cycle.getStartDate())) {
            throw new InvalidPatientDataException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio del período.");
        }

        cycle.finish(command.endDate());

        return saveMenstrualCyclePort.save(cycle);
    }
}