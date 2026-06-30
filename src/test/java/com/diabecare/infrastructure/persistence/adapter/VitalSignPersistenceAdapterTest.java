package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.VitalSign;
import com.diabecare.infrastructure.persistence.entity.VitalSignEntity;
import com.diabecare.infrastructure.persistence.mapper.VitalSignPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.VitalSignJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VitalSignPersistenceAdapter")
class VitalSignPersistenceAdapterTest {

    @Mock
    private VitalSignJpaRepository repository;

    private VitalSignPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new VitalSignPersistenceAdapter(repository, new VitalSignPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persiste el signo vital y retorna el dominio reconstruido")
        void persistsVitalSignAndReturnsReconstructedDomain() {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID())
                    .patientId(patientId)
                    .weightKg(BigDecimal.valueOf(70))
                    .measuredAt(LocalDateTime.now())
                    .build();

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            VitalSign result = adapter.save(vitalSign);

            assertThat(result.getWeightKg()).isEqualByComparingTo(BigDecimal.valueOf(70));
            verify(repository).save(any(VitalSignEntity.class));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna el signo vital convertido a dominio cuando existe")
        void returnsVitalSignConvertedToDomainWhenExists() {
            VitalSignEntity entity = validEntity();
            when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

            Optional<VitalSign> result = adapter.findById(entity.getId());

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findLatestByPatientId")
    class FindLatestByPatientId {

        @Test
        @DisplayName("retorna el signo vital más reciente convertido a dominio")
        void returnsLatestVitalSignConvertedToDomain() {
            when(repository.findFirstByPatientIdOrderByMeasuredAtDesc(patientId))
                    .thenReturn(Optional.of(validEntity()));

            Optional<VitalSign> result = adapter.findLatestByPatientId(patientId);

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findByPatientId")
    class FindByPatientId {

        @Test
        @DisplayName("retorna los signos vitales del paciente convertidos a dominio, limitados a 500")
        void returnsPatientVitalSignsConvertedToDomainLimitedTo500() {
            when(repository.findFirst500ByPatientIdOrderByMeasuredAtDesc(patientId))
                    .thenReturn(List.of(validEntity()));

            List<VitalSign> result = adapter.findByPatientId(patientId);

            assertThat(result).hasSize(1);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private VitalSignEntity validEntity() {
        return VitalSignEntity.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .weightKg(BigDecimal.valueOf(70))
                .measuredAt(LocalDateTime.now())
                .build();
    }
}