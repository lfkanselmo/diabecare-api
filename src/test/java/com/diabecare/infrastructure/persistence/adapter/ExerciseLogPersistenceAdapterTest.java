package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import com.diabecare.infrastructure.persistence.entity.ExerciseLogEntity;
import com.diabecare.infrastructure.persistence.mapper.ExerciseLogPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.ExerciseLogJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExerciseLogPersistenceAdapter")
class ExerciseLogPersistenceAdapterTest {

    @Mock
    private ExerciseLogJpaRepository repository;

    private ExerciseLogPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new ExerciseLogPersistenceAdapter(repository, new ExerciseLogPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persiste el ejercicio y retorna el dominio reconstruido")
        void persistsExerciseAndReturnsReconstructedDomain() {
            ExerciseLog log = ExerciseLog.create(
                    patientId, ExerciseType.RUNNING, ExerciseIntensity.HIGH,
                    45, "carrera", LocalDateTime.now().minusMinutes(5), BigDecimal.valueOf(400));

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ExerciseLog result = adapter.save(log);

            assertThat(result.getExerciseType()).isEqualTo(ExerciseType.RUNNING);
            assertThat(result.getCaloriesBurned()).isEqualByComparingTo(BigDecimal.valueOf(400));
            verify(repository).save(any(ExerciseLogEntity.class));
        }
    }

    @Nested
    @DisplayName("findByPatientIdAndDateRange")
    class FindByPatientIdAndDateRange {

        @Test
        @DisplayName("retorna los ejercicios del rango convertidos a dominio")
        void returnsExercisesInRangeConvertedToDomain() {
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();

            ExerciseLogEntity entity = ExerciseLogEntity.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .exerciseType("WALKING")
                    .intensity("LOW")
                    .durationMinutes(30)
                    .caloriesBurned(BigDecimal.valueOf(100))
                    .performedAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(repository.findByPatientIdAndPerformedAtBetweenOrderByPerformedAtDesc(patientId, from, to))
                    .thenReturn(List.of(entity));

            List<ExerciseLog> result = adapter.findByPatientIdAndDateRange(patientId, from, to);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getExerciseType()).isEqualTo(ExerciseType.WALKING);
        }

        @Test
        @DisplayName("retorna lista vacía cuando no hay ejercicios en el rango")
        void returnsEmptyListWhenNoExercisesInRange() {
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();

            when(repository.findByPatientIdAndPerformedAtBetweenOrderByPerformedAtDesc(patientId, from, to))
                    .thenReturn(List.of());

            List<ExerciseLog> result = adapter.findByPatientIdAndDateRange(patientId, from, to);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("retorna una página de ejercicios del rango convertidos a dominio")
        void returnsPagedExercisesInRangeConvertedToDomain() {
            LocalDateTime from = LocalDateTime.now().minusDays(7);
            LocalDateTime to = LocalDateTime.now();
            Pageable pageable = PageRequest.of(0, 20);

            ExerciseLogEntity entity = ExerciseLogEntity.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .exerciseType("WALKING")
                    .intensity("LOW")
                    .durationMinutes(30)
                    .caloriesBurned(BigDecimal.valueOf(100))
                    .performedAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(repository.findByPatientIdAndPerformedAtBetweenOrderByPerformedAtDesc(
                    patientId, from, to, pageable))
                    .thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

            Page<ExerciseLog> result = adapter.findByPatientIdAndDateRange(patientId, from, to, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }
}