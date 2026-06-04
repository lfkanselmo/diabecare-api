package com.diabecare.application.usecase;

import com.diabecare.application.port.in.CalculateInsulinDoseUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CalculateInsulinDoseUseCaseImpl implements CalculateInsulinDoseUseCase {

    private final LoadPatientPort loadPatientPort;

    @Override
    public Result calculate(Command command) {
        Patient patient = loadPatientPort.findById(command.patientId())
                .orElseThrow(() -> new PatientNotFoundException(
                        command.patientId().toString()));

        if (patient.getInsulinSensitivityFactor() == null
                || patient.getInsulinToCarbRatio() == null
                || patient.getTargetGlucoseForCorrection() == null) {
            throw new InvalidPatientDataException(
                    "Configura tu perfil de insulina antes de usar la calculadora.");
        }

        BigDecimal correctionDose = calculateCorrectionDose(
                command.currentGlucose(),
                patient.getTargetGlucoseForCorrection(),
                patient.getInsulinSensitivityFactor());

        BigDecimal mealDose = command.beforeMeal() && command.carbsToEat() != null
                ? command.carbsToEat().divide(
                patient.getInsulinToCarbRatio(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal total = correctionDose.add(mealDose)
                .max(BigDecimal.ZERO)
                .setScale(1, RoundingMode.HALF_UP);

        String explanation = buildExplanation(
                command.currentGlucose(),
                patient.getTargetGlucoseForCorrection(),
                correctionDose, mealDose, total,
                command.carbsToEat(), command.beforeMeal());

        return new Result(
                correctionDose.max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP),
                mealDose.setScale(1, RoundingMode.HALF_UP),
                total,
                explanation);
    }

    private BigDecimal calculateCorrectionDose(BigDecimal currentGlucose,
                                               BigDecimal targetGlucose,
                                               BigDecimal sensitivityFactor) {
        return currentGlucose.subtract(targetGlucose)
                .divide(sensitivityFactor, 2, RoundingMode.HALF_UP);
    }

    private String buildExplanation(BigDecimal current, BigDecimal target,
                                    BigDecimal correction, BigDecimal meal,
                                    BigDecimal total, BigDecimal carbs,
                                    boolean beforeMeal) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Glucosa actual: %.0f mg/dL → objetivo: %.0f mg/dL. ",
                current.doubleValue(), target.doubleValue()));

        if (correction.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Dosis de corrección: %.1f U. ", correction.doubleValue()));
        } else {
            sb.append("No se requiere corrección. ");
        }

        if (beforeMeal && carbs != null && carbs.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("Dosis para %.0f g de carbohidratos: %.1f U. ",
                    carbs.doubleValue(), meal.doubleValue()));
        }

        sb.append(String.format("Dosis total sugerida: %.1f U.", total.doubleValue()));
        return sb.toString();
    }
}