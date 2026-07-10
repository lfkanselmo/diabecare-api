package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.infrastructure.persistence.entity.MenstrualCycleEntity;
import com.diabecare.infrastructure.persistence.mapper.MenstrualCyclePersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.MenstrualCycleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenstrualCyclePersistenceAdapter")
class MenstrualCyclePersistenceAdapterTest {

    @Mock
    private MenstrualCycleJpaRepository repository;

    private MenstrualCyclePersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new MenstrualCyclePersistenceAdapter(repository, new MenstrualCyclePersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persiste el ciclo y retorna el dominio reconstruido")
        void persistsCycleAndReturnsReconstructedDomain() {
            MenstrualCycle cycle = MenstrualCycle.startNewCycle(
                    patientId, LocalDate.of(2026, 6, 1), null);

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            MenstrualCycle result = adapter.save(cycle);

            assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2026, 6, 1));
            verify(repository).save(any(MenstrualCycleEntity.class));
        }
    }

    @Nested
    @DisplayName("findByPatientId")
    class FindByPatientId {

        @Test
        @DisplayName("retorna todos los ciclos del paciente convertidos a dominio")
        void returnsAllPatientCyclesConvertedToDomain() {
            when(repository.findByPatientIdOrderByStartDateDesc(patientId))
                    .thenReturn(List.of(validEntity()));

            List<MenstrualCycle> result = adapter.findByPatientId(patientId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findLatestByPatientId")
    class FindLatestByPatientId {

        @Test
        @DisplayName("retorna el ciclo más reciente convertido a dominio")
        void returnsLatestCycleConvertedToDomain() {
            when(repository.findFirstByPatientIdOrderByStartDateDesc(patientId))
                    .thenReturn(Optional.of(validEntity()));

            Optional<MenstrualCycle> result = adapter.findLatestByPatientId(patientId);

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el paciente no tiene ciclos")
        void returnsEmptyWhenPatientHasNoCycles() {
            when(repository.findFirstByPatientIdOrderByStartDateDesc(patientId))
                    .thenReturn(Optional.empty());

            Optional<MenstrualCycle> result = adapter.findLatestByPatientId(patientId);

            assertThat(result).isEmpty();
        }
    }


    private MenstrualCycleEntity validEntity() {
        return MenstrualCycleEntity.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .startDate(LocalDate.of(2026, 6, 1))
                .build();
    }
}
