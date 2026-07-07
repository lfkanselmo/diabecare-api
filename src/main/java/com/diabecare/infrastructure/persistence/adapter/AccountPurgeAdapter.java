package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.PurgeAccountDataPort;
import com.diabecare.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Purga en firme (Ley 1581 de 2012 — derecho de cancelación) todos los datos
 * clínicos y personales de una cuenta, en el orden que exigen las llaves
 * foráneas: primero las tablas hijas de {@code patients} (algunas, como
 * {@code push_subscriptions} y {@code audit_log}, ya tienen ON DELETE CASCADE
 * y se limpian solas al borrar el paciente), luego el propio paciente, y por
 * último los datos colgados directamente de {@code users}.
 * <p>
 * <b>Mantenimiento:</b> toda tabla nueva con una FK a {@code patients(id)} o
 * {@code users(id)} sin ON DELETE CASCADE debe agregarse aquí explícitamente
 * — de lo contrario, el borrado del paciente/usuario falla por violación de
 * llave foránea, o peor, la fila queda huérfana si la FK es nullable.
 */
@Component
@RequiredArgsConstructor
public class AccountPurgeAdapter implements PurgeAccountDataPort {

    private final GlucoseReadingJpaRepository glucoseReadingJpaRepository;
    private final MealEntryJpaRepository mealEntryJpaRepository;
    private final VitalSignJpaRepository vitalSignJpaRepository;
    private final MedicationJpaRepository medicationJpaRepository;
    private final ExerciseLogJpaRepository exerciseLogJpaRepository;
    private final MenstrualCycleJpaRepository menstrualCycleJpaRepository;
    private final CaregiverInviteJpaRepository caregiverInviteJpaRepository;
    private final CaregiverLinkJpaRepository caregiverLinkJpaRepository;
    private final PatientJpaRepository patientJpaRepository;
    private final RefreshTokenJpaRepository refreshTokenJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    @Transactional
    public void purgeAllDataFor(UUID userId, UUID patientId) {
        if (patientId != null) {
            glucoseReadingJpaRepository.deleteByPatientId(patientId);
            mealEntryJpaRepository.deleteByPatientId(patientId);
            vitalSignJpaRepository.deleteByPatientId(patientId);
            medicationJpaRepository.deleteByPatientId(patientId);
            exerciseLogJpaRepository.deleteByPatientId(patientId);
            menstrualCycleJpaRepository.deleteByPatientId(patientId);
            caregiverInviteJpaRepository.deleteByPatientId(patientId);
            caregiverLinkJpaRepository.deleteByPatientId(patientId);
        }

        // Enlaces/invitaciones donde esta cuenta actuaba como cuidador de OTRO
        // paciente (no cuelgan de patientId, sino de este userId).
        caregiverLinkJpaRepository.deleteByCaregiverUserId(userId);
        caregiverInviteJpaRepository.clearRedeemedByUserId(userId);

        if (patientId != null) {
            patientJpaRepository.deleteById(patientId);
        }

        refreshTokenJpaRepository.deleteByUserId(userId);
        userJpaRepository.deleteById(userId);
    }
}
