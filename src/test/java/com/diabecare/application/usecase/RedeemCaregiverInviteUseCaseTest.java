package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RedeemCaregiverInviteUseCase;
import com.diabecare.application.port.out.CaregiverInvitePort;
import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SaveCaregiverLinkPort;
import com.diabecare.domain.exception.InvalidCaregiverInviteException;
import com.diabecare.domain.model.CaregiverInvite;
import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedeemCaregiverInviteUseCaseImpl")
class RedeemCaregiverInviteUseCaseTest {

    @Mock
    private CaregiverInvitePort caregiverInvitePort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private LoadCaregiverLinkPort loadCaregiverLinkPort;
    @Mock
    private SaveCaregiverLinkPort saveCaregiverLinkPort;
    @Mock
    private SaveAuditLogPort saveAuditLogPort;

    private RedeemCaregiverInviteUseCaseImpl useCase;

    private final UUID patientOwnerUserId = UUID.randomUUID();
    private final UUID caregiverUserId = UUID.randomUUID();
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RedeemCaregiverInviteUseCaseImpl(
                caregiverInvitePort, loadPatientPort, loadCaregiverLinkPort,
                saveCaregiverLinkPort, saveAuditLogPort, new AuditService());
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("crea un enlace activo cuando la invitación es válida y el cuidador no es el dueño")
        void createsActiveLinkWhenInviteIsValidAndCaregiverIsNotOwner() {
            CaregiverInvite invite = validInvite();
            Patient patient = validPatient(patientOwnerUserId);

            when(caregiverInvitePort.findByRawCode("ABCD-1234")).thenReturn(Optional.of(invite));
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
            when(loadCaregiverLinkPort.existsActive(patientId, caregiverUserId)).thenReturn(false);
            when(saveCaregiverLinkPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RedeemCaregiverInviteUseCase.Result result = useCase.execute(
                    new RedeemCaregiverInviteUseCase.Command("ABCD-1234", caregiverUserId));

            assertThat(result.patientId()).isEqualTo(patientId);
            assertThat(result.patientFullName()).isEqualTo("Ana García");

            verify(caregiverInvitePort).markRedeemed(invite.getId(), caregiverUserId);

            var captor = org.mockito.ArgumentCaptor.forClass(CaregiverLink.class);
            verify(saveCaregiverLinkPort).save(captor.capture());
            assertThat(captor.getValue().getPatientId()).isEqualTo(patientId);
            assertThat(captor.getValue().getCaregiverUserId()).isEqualTo(caregiverUserId);
            assertThat(captor.getValue().isActive()).isTrue();

            verify(saveAuditLogPort).save(any());
        }

        @Test
        @DisplayName("lanza InvalidCaregiverInviteException cuando el código no existe")
        void throwsWhenCodeDoesNotExist() {
            when(caregiverInvitePort.findByRawCode("NOPE-CODE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(
                    new RedeemCaregiverInviteUseCase.Command("NOPE-CODE", caregiverUserId)))
                    .isInstanceOf(InvalidCaregiverInviteException.class);

            verifyNoInteractions(saveCaregiverLinkPort);
        }

        @Test
        @DisplayName("lanza InvalidCaregiverInviteException cuando la invitación ya expiró")
        void throwsWhenInviteIsExpired() {
            CaregiverInvite expired = CaregiverInvite.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .expiresAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(caregiverInvitePort.findByRawCode("ABCD-1234")).thenReturn(Optional.of(expired));

            assertThatThrownBy(() -> useCase.execute(
                    new RedeemCaregiverInviteUseCase.Command("ABCD-1234", caregiverUserId)))
                    .isInstanceOf(InvalidCaregiverInviteException.class);

            verifyNoInteractions(saveCaregiverLinkPort);
        }

        @Test
        @DisplayName("lanza InvalidCaregiverInviteException cuando el cuidador es el propio dueño del paciente")
        void throwsWhenCaregiverIsThePatientOwner() {
            CaregiverInvite invite = validInvite();
            Patient patient = validPatient(caregiverUserId);

            when(caregiverInvitePort.findByRawCode("ABCD-1234")).thenReturn(Optional.of(invite));
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            assertThatThrownBy(() -> useCase.execute(
                    new RedeemCaregiverInviteUseCase.Command("ABCD-1234", caregiverUserId)))
                    .isInstanceOf(InvalidCaregiverInviteException.class);

            verify(caregiverInvitePort, never()).markRedeemed(any(), any());
            verifyNoInteractions(saveCaregiverLinkPort);
        }

        @Test
        @DisplayName("lanza InvalidCaregiverInviteException cuando el cuidador ya tiene acceso activo")
        void throwsWhenCaregiverAlreadyHasActiveAccess() {
            CaregiverInvite invite = validInvite();
            Patient patient = validPatient(patientOwnerUserId);

            when(caregiverInvitePort.findByRawCode("ABCD-1234")).thenReturn(Optional.of(invite));
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));
            when(loadCaregiverLinkPort.existsActive(patientId, caregiverUserId)).thenReturn(true);

            assertThatThrownBy(() -> useCase.execute(
                    new RedeemCaregiverInviteUseCase.Command("ABCD-1234", caregiverUserId)))
                    .isInstanceOf(InvalidCaregiverInviteException.class);

            verify(caregiverInvitePort, never()).markRedeemed(any(), any());
            verifyNoInteractions(saveCaregiverLinkPort);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private CaregiverInvite validInvite() {
        return CaregiverInvite.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }

    private Patient validPatient(UUID ownerUserId) {
        return Patient.builder()
                .patientId(patientId)
                .userId(ownerUserId)
                .fullName("Ana García")
                .dateOfBirth(LocalDate.of(1990, 5, 10))
                .diabetesType(DiabetesType.TYPE_1)
                .diagnosisDate(LocalDate.of(2010, 1, 1))
                .heightCm(BigDecimal.valueOf(165))
                .build();
    }
}
