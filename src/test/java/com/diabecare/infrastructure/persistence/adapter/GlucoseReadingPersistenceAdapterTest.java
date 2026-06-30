package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.infrastructure.persistence.entity.GlucoseReadingEntity;
import com.diabecare.infrastructure.persistence.mapper.GlucoseReadingPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.GlucoseReadingJpaRepository;
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
@DisplayName("GlucoseReadingPersistenceAdapter")
class GlucoseReadingPersistenceAdapterTest {

    @Mock
    private GlucoseReadingJpaRepository repository;

    private GlucoseReadingPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new GlucoseReadingPersistenceAdapter(repository, new GlucoseReadingPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persiste la lectura y retorna el dominio reconstruido")
        void persistsReadingAndReturnsReconstructedDomain() {
            GlucoseReading reading = GlucoseReading.create(
                    patientId, BigDecimal.valueOf(120), GlucoseUnit.MG_DL,
                    ReadingType.FASTING, LocalDateTime.now().minusMinutes(5), null, null);

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            GlucoseReading result = adapter.save(reading);

            assertThat(result.getValue()).isEqualByComparingTo(BigDecimal.valueOf(120));
            verify(repository).save(any(GlucoseReadingEntity.class));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna la lectura convertida a dominio cuando existe")
        void returnsReadingConvertedToDomainWhenExists() {
            GlucoseReadingEntity entity = validEntity();
            when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

            Optional<GlucoseReading> result = adapter.findById(entity.getId());

            assertThat(result).isPresent();
        }

        @Test
        @DisplayName("retorna Optional vacío cuando no existe")
        void returnsEmptyWhenNotExists() {
            UUID readingId = UUID.randomUUID();
            when(repository.findById(readingId)).thenReturn(Optional.empty());

            Optional<GlucoseReading> result = adapter.findById(readingId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findLatestByPatientId")
    class FindLatestByPatientId {

        @Test
        @DisplayName("retorna la lectura más reciente convertida a dominio")
        void returnsLatestReadingConvertedToDomain() {
            GlucoseReadingEntity entity = validEntity();
            when(repository.findFirstByPatientIdOrderByMeasuredAtDesc(patientId))
                    .thenReturn(Optional.of(entity));

            Optional<GlucoseReading> result = adapter.findLatestByPatientId(patientId);

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findByPatientIdAndDateRange")
    class FindByPatientIdAndDateRange {

        @Test
        @DisplayName("retorna las lecturas del rango convertidas a dominio")
        void returnsReadingsInRangeConvertedToDomain() {
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();

            when(repository.findByPatientIdAndMeasuredAtBetweenOrderByMeasuredAtDesc(patientId, from, to))
                    .thenReturn(List.of(validEntity()));

            List<GlucoseReading> result = adapter.findByPatientIdAndDateRange(patientId, from, to);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("delega la eliminación al repositorio")
        void delegatesDeletionToRepository() {
            UUID readingId = UUID.randomUUID();

            adapter.deleteById(readingId);

            verify(repository).deleteById(readingId);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private GlucoseReadingEntity validEntity() {
        return GlucoseReadingEntity.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .value(BigDecimal.valueOf(100))
                .unit("MG_DL")
                .readingType("RANDOM")
                .measuredAt(LocalDateTime.now())
                .build();
    }
}