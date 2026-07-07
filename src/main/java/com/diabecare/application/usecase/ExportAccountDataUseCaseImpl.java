package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ExportAccountDataUseCase;
import com.diabecare.application.port.out.*;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.AccountExportData;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.model.User;
import com.diabecare.domain.service.AccountExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ExportAccountDataUseCaseImpl implements ExportAccountDataUseCase {

    // Límites amplios para traer TODO el historial, ya que estos puertos de
    // lectura fueron diseñados para consultas por rango (reportes/gráficas),
    // no existe un "buscar todo" dedicado para estas entidades.
    private static final LocalDateTime FAR_PAST_DATETIME = LocalDateTime.of(2000, 1, 1, 0, 0);
    private static final LocalDate FAR_PAST_DATE = LocalDate.of(2000, 1, 1);

    private final LoadUserPort loadUserPort;
    private final LoadPatientPort loadPatientPort;
    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final LoadMealEntryPort loadMealEntryPort;
    private final LoadVitalSignPort loadVitalSignPort;
    private final LoadMedicationPort loadMedicationPort;
    private final LoadExerciseLogPort loadExerciseLogPort;
    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final LoadCaregiverLinkPort loadCaregiverLinkPort;
    private final AccountExportService accountExportService;

    @Override
    public byte[] exportAsJson(UUID userId) {
        User user = loadUserPort.findById(userId)
                .orElseThrow(() -> new PatientNotFoundException(userId.toString()));

        Patient patient = loadPatientPort.findByUserId(userId)
                .orElseThrow(() -> new PatientNotFoundException(userId.toString()));

        UUID patientId = patient.getPatientId();
        LocalDateTime now = LocalDateTime.now();

        AccountExportData data = AccountExportData.builder()
                .email(user.getEmail())
                .accountCreatedAt(user.getCreatedAt())
                .termsAcceptedAt(user.getTermsAcceptedAt())
                .termsVersion(user.getTermsVersion())
                .patient(patient)
                .glucoseReadings(loadGlucoseReadingPort.findByPatientIdAndDateRange(
                        patientId, FAR_PAST_DATETIME, now))
                .mealEntries(loadMealEntryPort.findByPatientIdAndDateRange(
                        patientId, FAR_PAST_DATE, LocalDate.now()))
                .vitalSigns(loadVitalSignPort.findByPatientId(patientId))
                .medications(loadMedicationPort.findAllByPatientId(patientId))
                .exerciseLogs(loadExerciseLogPort.findByPatientIdAndDateRange(
                        patientId, FAR_PAST_DATETIME, now))
                .menstrualCycles(loadMenstrualCyclePort.findByPatientId(patientId))
                .caregiversWithAccessToMyData(loadCaregiverLinkPort.findActiveByPatientId(patientId))
                .patientsICareFor(loadCaregiverLinkPort.findActiveByCaregiverUserId(userId))
                .build();

        String json = accountExportService.toJson(data);
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
