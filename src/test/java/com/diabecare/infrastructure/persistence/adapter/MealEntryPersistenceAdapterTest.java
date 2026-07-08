package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.MealEntry;
import com.diabecare.domain.model.MealItem;
import com.diabecare.domain.model.MealType;
import com.diabecare.infrastructure.persistence.entity.MealEntryEntity;
import com.diabecare.infrastructure.persistence.mapper.MealEntryPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.MealEntryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MealEntryPersistenceAdapter")
class MealEntryPersistenceAdapterTest {

    @Mock
    private MealEntryJpaRepository repository;

    private MealEntryPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new MealEntryPersistenceAdapter(repository, new MealEntryPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("asocia cada item a la comida padre antes de guardar")
        void associatesEachItemToParentMealBeforeSaving() {
            MealEntry meal = MealEntry.create(
                    patientId, MealType.BREAKFAST, LocalDateTime.now().minusMinutes(5), null);
            meal.addItem(MealItem.create("Pan", BigDecimal.valueOf(50),
                    BigDecimal.valueOf(100), BigDecimal.valueOf(20), null, null, null));
            meal.addItem(MealItem.create("Huevo", BigDecimal.valueOf(60),
                    BigDecimal.valueOf(80), BigDecimal.valueOf(1), null, null, null));

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.save(meal);

            ArgumentCaptor<MealEntryEntity> captor = ArgumentCaptor.forClass(MealEntryEntity.class);
            verify(repository).save(captor.capture());

            MealEntryEntity savedEntity = captor.getValue();
            assertThat(savedEntity.getItems()).hasSize(2);
            assertThat(savedEntity.getItems().get(0).getMealEntry()).isSameAs(savedEntity);
            assertThat(savedEntity.getItems().get(1).getMealEntry()).isSameAs(savedEntity);
        }

        @Test
        @DisplayName("guarda correctamente una comida sin items")
        void savesMealWithoutItemsCorrectly() {
            MealEntry meal = MealEntry.create(
                    patientId, MealType.SNACK, LocalDateTime.now().minusMinutes(5), null);

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.save(meal);

            ArgumentCaptor<MealEntryEntity> captor = ArgumentCaptor.forClass(MealEntryEntity.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna la comida convertida a dominio cuando existe")
        void returnsMealConvertedToDomainWhenExists() {
            MealEntryEntity entity = validEntity();
            when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

            Optional<MealEntry> result = adapter.findById(entity.getId());

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findByPatientIdAndDate")
    class FindByPatientIdAndDate {

        @Test
        @DisplayName("consulta usando el rango horario completo del día (00:00:00 a 23:59:59)")
        void queriesUsingFullDayTimeRange() {
            LocalDate date = LocalDate.of(2026, 6, 15);
            when(repository.findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(any(), any(), any()))
                    .thenReturn(List.of());

            adapter.findByPatientIdAndDate(patientId, date);

            verify(repository).findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
                    patientId, date.atStartOfDay(), date.atTime(23, 59, 59));
        }
    }

    @Nested
    @DisplayName("findByPatientIdAndDateRange")
    class FindByPatientIdAndDateRange {

        @Test
        @DisplayName("consulta usando el inicio del primer día y el final del último día del rango")
        void queriesUsingStartOfFirstDayAndEndOfLastDay() {
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 7);
            when(repository.findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(any(), any(), any()))
                    .thenReturn(List.of());

            adapter.findByPatientIdAndDateRange(patientId, from, to);

            verify(repository).findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
                    patientId, from.atStartOfDay(), to.atTime(23, 59, 59));
        }

        @Test
        @DisplayName("consulta paginada usando el inicio del primer día y el final del último día del rango")
        void queriesPagedUsingStartOfFirstDayAndEndOfLastDay() {
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 7);
            Pageable pageable = PageRequest.of(0, 20);
            Page<MealEntryEntity> entityPage = new PageImpl<>(List.of(validEntity()), pageable, 1);

            when(repository.findByPatientIdAndConsumedAtBetweenOrderByConsumedAtDesc(
                    patientId, from.atStartOfDay(), to.atTime(23, 59, 59), pageable))
                    .thenReturn(entityPage);

            Page<MealEntry> result = adapter.findByPatientIdAndDateRange(patientId, from, to, pageable);

            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("delega la eliminación al repositorio")
        void delegatesDeletionToRepository() {
            UUID mealId = UUID.randomUUID();

            adapter.deleteById(mealId);

            verify(repository).deleteById(mealId);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private MealEntryEntity validEntity() {
        return MealEntryEntity.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .mealType("LUNCH")
                .consumedAt(LocalDateTime.now())
                .build();
    }
}