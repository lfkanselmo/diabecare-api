package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.infrastructure.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountPurgeAdapter")
class AccountPurgeAdapterTest {

    @Mock private GlucoseReadingJpaRepository glucoseReadingJpaRepository;
    @Mock private MealEntryJpaRepository mealEntryJpaRepository;
    @Mock private VitalSignJpaRepository vitalSignJpaRepository;
    @Mock private MedicationJpaRepository medicationJpaRepository;
    @Mock private ExerciseLogJpaRepository exerciseLogJpaRepository;
    @Mock private MenstrualCycleJpaRepository menstrualCycleJpaRepository;
    @Mock private CaregiverInviteJpaRepository caregiverInviteJpaRepository;
    @Mock private CaregiverLinkJpaRepository caregiverLinkJpaRepository;
    @Mock private PatientJpaRepository patientJpaRepository;
    @Mock private RefreshTokenJpaRepository refreshTokenJpaRepository;
    @Mock private PasswordResetTokenJpaRepository passwordResetTokenJpaRepository;
    @Mock private UserJpaRepository userJpaRepository;

    private AccountPurgeAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AccountPurgeAdapter(
                glucoseReadingJpaRepository, mealEntryJpaRepository, vitalSignJpaRepository,
                medicationJpaRepository, exerciseLogJpaRepository, menstrualCycleJpaRepository,
                caregiverInviteJpaRepository, caregiverLinkJpaRepository, patientJpaRepository,
                refreshTokenJpaRepository, passwordResetTokenJpaRepository, userJpaRepository);
    }

    @Test
    @DisplayName("borra todos los datos clinicos por patientId y luego al paciente y al usuario")
    void deletesAllClinicalDataByPatientIdThenPatientAndUser() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        adapter.purgeAllDataFor(userId, patientId);

        verify(glucoseReadingJpaRepository).deleteByPatientId(patientId);
        verify(mealEntryJpaRepository).deleteByPatientId(patientId);
        verify(vitalSignJpaRepository).deleteByPatientId(patientId);
        verify(medicationJpaRepository).deleteByPatientId(patientId);
        verify(exerciseLogJpaRepository).deleteByPatientId(patientId);
        verify(menstrualCycleJpaRepository).deleteByPatientId(patientId);
        verify(caregiverInviteJpaRepository).deleteByPatientId(patientId);
        verify(caregiverLinkJpaRepository).deleteByPatientId(patientId);
        verify(patientJpaRepository).deleteById(patientId);

        verify(caregiverLinkJpaRepository).deleteByCaregiverUserId(userId);
        verify(caregiverInviteJpaRepository).clearRedeemedByUserId(userId);
        verify(refreshTokenJpaRepository).deleteByUserId(userId);
        verify(passwordResetTokenJpaRepository).deleteByUserId(userId);
        verify(userJpaRepository).deleteById(userId);
    }

    @Test
    @DisplayName("cuando patientId es nulo, omite las tablas clinicas y solo borra lo colgado del usuario")
    void whenPatientIdIsNullSkipsClinicalTablesAndOnlyDeletesUserScopedData() {
        UUID userId = UUID.randomUUID();

        adapter.purgeAllDataFor(userId, null);

        verifyNoInteractions(glucoseReadingJpaRepository, mealEntryJpaRepository, vitalSignJpaRepository,
                medicationJpaRepository, exerciseLogJpaRepository, menstrualCycleJpaRepository);
        verify(caregiverInviteJpaRepository, never()).deleteByPatientId(any());
        verify(caregiverLinkJpaRepository, never()).deleteByPatientId(any());
        verify(patientJpaRepository, never()).deleteById(any());

        verify(caregiverLinkJpaRepository).deleteByCaregiverUserId(userId);
        verify(caregiverInviteJpaRepository).clearRedeemedByUserId(userId);
        verify(refreshTokenJpaRepository).deleteByUserId(userId);
        verify(passwordResetTokenJpaRepository).deleteByUserId(userId);
        verify(userJpaRepository).deleteById(userId);
    }
}
