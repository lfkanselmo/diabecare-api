package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.model.CaregiverLinkStatus;
import com.diabecare.infrastructure.persistence.entity.CaregiverLinkEntity;
import com.diabecare.infrastructure.persistence.repository.CaregiverLinkJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CaregiverLinkPersistenceAdapter")
class CaregiverLinkPersistenceAdapterTest {

    @Mock
    private CaregiverLinkJpaRepository repository;

    private CaregiverLinkPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();
    private final UUID caregiverUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new CaregiverLinkPersistenceAdapter(repository);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("guarda el enlace y lo retorna mapeado correctamente")
        void savesLinkAndReturnsMappedCorrectly() {
            CaregiverLink link = CaregiverLink.create(patientId, caregiverUserId);
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CaregiverLink saved = adapter.save(link);

            assertThat(saved.getId()).isEqualTo(link.getId());
            assertThat(saved.getPatientId()).isEqualTo(patientId);
            assertThat(saved.getCaregiverUserId()).isEqualTo(caregiverUserId);
            assertThat(saved.getStatus()).isEqualTo(CaregiverLinkStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna el enlace mapeado cuando existe")
        void returnsLinkMappedWhenExists() {
            UUID linkId = UUID.randomUUID();
            CaregiverLinkEntity entity = validEntity(linkId);
            when(repository.findById(linkId)).thenReturn(Optional.of(entity));

            Optional<CaregiverLink> result = adapter.findById(linkId);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(linkId);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando no existe")
        void returnsEmptyWhenDoesNotExist() {
            UUID linkId = UUID.randomUUID();
            when(repository.findById(linkId)).thenReturn(Optional.empty());

            assertThat(adapter.findById(linkId)).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsActive")
    class ExistsActive {

        @Test
        @DisplayName("delega en el repositorio con el status ACTIVE")
        void delegatesToRepositoryWithActiveStatus() {
            when(repository.existsByPatientIdAndCaregiverUserIdAndStatus(
                    patientId, caregiverUserId, "ACTIVE")).thenReturn(true);

            assertThat(adapter.existsActive(patientId, caregiverUserId)).isTrue();
        }
    }

    @Nested
    @DisplayName("findActiveByPatientId")
    class FindActiveByPatientId {

        @Test
        @DisplayName("retorna los enlaces activos del paciente mapeados correctamente")
        void returnsActiveLinksForPatientMappedCorrectly() {
            CaregiverLinkEntity entity = validEntity(UUID.randomUUID());
            when(repository.findByPatientIdAndStatus(patientId, "ACTIVE")).thenReturn(List.of(entity));

            List<CaregiverLink> result = adapter.findActiveByPatientId(patientId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPatientId()).isEqualTo(patientId);
        }
    }

    @Nested
    @DisplayName("findActiveByCaregiverUserId")
    class FindActiveByCaregiverUserId {

        @Test
        @DisplayName("retorna los pacientes a los que el cuidador tiene acceso, mapeados correctamente")
        void returnsPatientsCaregiverHasAccessToMappedCorrectly() {
            CaregiverLinkEntity entity = validEntity(UUID.randomUUID());
            when(repository.findByCaregiverUserIdAndStatus(caregiverUserId, "ACTIVE")).thenReturn(List.of(entity));

            List<CaregiverLink> result = adapter.findActiveByCaregiverUserId(caregiverUserId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getCaregiverUserId()).isEqualTo(caregiverUserId);
        }
    }

    private CaregiverLinkEntity validEntity(UUID linkId) {
        return CaregiverLinkEntity.builder()
                .id(linkId)
                .patientId(patientId)
                .caregiverUserId(caregiverUserId)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
