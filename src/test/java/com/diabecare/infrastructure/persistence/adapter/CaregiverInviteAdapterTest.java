package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.CaregiverInvitePort;
import com.diabecare.domain.model.CaregiverInvite;
import com.diabecare.infrastructure.persistence.entity.CaregiverInviteEntity;
import com.diabecare.infrastructure.persistence.repository.CaregiverInviteJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaregiverInviteAdapter")
class CaregiverInviteAdapterTest {

    @Mock
    private CaregiverInviteJpaRepository repository;

    private CaregiverInviteAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new CaregiverInviteAdapter(repository);
    }

    @Nested
    @DisplayName("issue")
    class Issue {

        @Test
        @DisplayName("genera un código con formato XXXX-XXXX cuyo hash no coincide con el código crudo")
        void generatesFormattedCodeWithHashDifferentFromRawCode() {
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CaregiverInvitePort.IssuedInvite result = adapter.issue(patientId, expiresAt);

            assertThat(result.rawCode()).matches("[A-Z0-9]{4}-[A-Z0-9]{4}");
            assertThat(result.expiresAt()).isEqualTo(expiresAt);

            ArgumentCaptor<CaregiverInviteEntity> captor = ArgumentCaptor.forClass(CaregiverInviteEntity.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getCodeHash()).isNotEqualTo(result.rawCode());
            assertThat(captor.getValue().getPatientId()).isEqualTo(patientId);
            assertThat(captor.getValue().getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("genera códigos distintos en cada llamada")
        void generatesDifferentCodesOnEachCall() {
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CaregiverInvitePort.IssuedInvite first = adapter.issue(patientId, LocalDateTime.now().plusDays(7));
            CaregiverInvitePort.IssuedInvite second = adapter.issue(patientId, LocalDateTime.now().plusDays(7));

            assertThat(first.rawCode()).isNotEqualTo(second.rawCode());
        }
    }

    @Nested
    @DisplayName("findByRawCode")
    class FindByRawCode {

        @Test
        @DisplayName("encuentra la invitación ignorando mayúsculas/minúsculas y el guion de formato")
        void findsInviteIgnoringCaseAndFormattingHyphen() {
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            CaregiverInvitePort.IssuedInvite issued = adapter.issue(patientId, LocalDateTime.now().plusDays(7));

            ArgumentCaptor<CaregiverInviteEntity> captor = ArgumentCaptor.forClass(CaregiverInviteEntity.class);
            verify(repository).save(captor.capture());
            String storedHash = captor.getValue().getCodeHash();

            when(repository.findByCodeHash(storedHash)).thenReturn(Optional.of(captor.getValue()));

            Optional<CaregiverInvite> result = adapter.findByRawCode(issued.rawCode().toLowerCase());

            assertThat(result).isPresent();
            assertThat(result.get().getPatientId()).isEqualTo(patientId);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el código no existe")
        void returnsEmptyWhenCodeDoesNotExist() {
            when(repository.findByCodeHash(any())).thenReturn(Optional.empty());

            Optional<CaregiverInvite> result = adapter.findByRawCode("NOPE-CODE");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("markRedeemed")
    class MarkRedeemed {

        @Test
        @DisplayName("marca la invitación como redimida con la fecha y el usuario correctos")
        void marksInviteAsRedeemedWithCorrectDateAndUser() {
            UUID inviteId = UUID.randomUUID();
            UUID redeemedByUserId = UUID.randomUUID();
            CaregiverInviteEntity entity = CaregiverInviteEntity.builder().id(inviteId).build();

            when(repository.findById(inviteId)).thenReturn(Optional.of(entity));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.markRedeemed(inviteId, redeemedByUserId);

            assertThat(entity.getRedeemedAt()).isNotNull();
            assertThat(entity.getRedeemedByUserId()).isEqualTo(redeemedByUserId);
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("no hace nada cuando la invitación no existe")
        void doesNothingWhenInviteDoesNotExist() {
            UUID inviteId = UUID.randomUUID();
            when(repository.findById(inviteId)).thenReturn(Optional.empty());

            adapter.markRedeemed(inviteId, UUID.randomUUID());

            verify(repository, never()).save(any());
        }
    }
}
