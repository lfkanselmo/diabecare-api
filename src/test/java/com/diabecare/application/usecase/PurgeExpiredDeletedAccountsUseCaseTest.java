package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.PurgeAccountDataPort;
import com.diabecare.domain.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PurgeExpiredDeletedAccountsUseCaseImpl")
class PurgeExpiredDeletedAccountsUseCaseTest {

    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private PurgeAccountDataPort purgeAccountDataPort;

    private PurgeExpiredDeletedAccountsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new PurgeExpiredDeletedAccountsUseCaseImpl(loadUserPort, loadPatientPort, purgeAccountDataPort);
    }

    @Test
    @DisplayName("purga cada cuenta vencida junto con su patientId resuelto")
    void purgesEachExpiredAccountWithItsResolvedPatientId() {
        UUID userId = UUID.randomUUID();
        Patient patient = Patient.builder().patientId(UUID.randomUUID()).userId(userId).build();

        when(loadUserPort.findIdsDeletedBefore(any())).thenReturn(List.of(userId));
        when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.of(patient));

        int result = useCase.execute();

        assertThat(result).isEqualTo(1);
        verify(purgeAccountDataPort).purgeAllDataFor(userId, patient.getPatientId());
    }

    @Test
    @DisplayName("usa un cutoff de 30 dias de gracia hacia atras")
    void usesThirtyDayGracePeriodCutoff() {
        when(loadUserPort.findIdsDeletedBefore(any())).thenReturn(List.of());

        useCase.execute();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(loadUserPort).findIdsDeletedBefore(cutoffCaptor.capture());

        long daysAgo = java.time.Duration.between(cutoffCaptor.getValue(), LocalDateTime.now()).toDays();
        assertThat(daysAgo).isBetween(29L, 30L);
    }

    @Test
    @DisplayName("purga con patientId nulo cuando el usuario nunca tuvo perfil de paciente")
    void purgesWithNullPatientIdWhenUserNeverHadAPatientProfile() {
        UUID userId = UUID.randomUUID();
        when(loadUserPort.findIdsDeletedBefore(any())).thenReturn(List.of(userId));
        when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.empty());

        useCase.execute();

        verify(purgeAccountDataPort).purgeAllDataFor(userId, null);
    }

    @Test
    @DisplayName("retorna 0 y no purga nada cuando no hay cuentas vencidas")
    void returnsZeroAndPurgesNothingWhenNoExpiredAccounts() {
        when(loadUserPort.findIdsDeletedBefore(any())).thenReturn(List.of());

        int result = useCase.execute();

        assertThat(result).isZero();
        verifyNoInteractions(purgeAccountDataPort);
    }
}
