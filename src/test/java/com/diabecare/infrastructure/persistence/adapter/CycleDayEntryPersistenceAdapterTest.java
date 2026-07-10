package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CycleSymptom;
import com.diabecare.domain.model.CycleSymptomEntry;
import com.diabecare.domain.model.FlowIntensity;
import com.diabecare.domain.model.SymptomSeverity;
import com.diabecare.infrastructure.persistence.entity.CycleDayEntryEntity;
import com.diabecare.infrastructure.persistence.entity.CycleDaySymptomEntity;
import com.diabecare.infrastructure.persistence.mapper.CycleDayEntryPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.CycleDayEntryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CycleDayEntryPersistenceAdapter")
class CycleDayEntryPersistenceAdapterTest {

    @Mock
    private CycleDayEntryJpaRepository repository;

    private CycleDayEntryPersistenceAdapter adapter;
    private final UUID cycleId = UUID.randomUUID();
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new CycleDayEntryPersistenceAdapter(repository, new CycleDayEntryPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("genera un id nuevo cuando la entrada no tiene uno asignado")
        void generatesNewIdWhenMissing() {
            CycleDayEntry entry = CycleDayEntry.create(
                    cycleId, patientId, LocalDate.of(2026, 6, 1), FlowIntensity.MODERATE, null, null);

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.save(entry);

            ArgumentCaptor<CycleDayEntryEntity> captor = ArgumentCaptor.forClass(CycleDayEntryEntity.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getId()).isNotNull();
        }

        @Test
        @DisplayName("convierte cada síntoma del dominio a una entidad de síntoma asociada")
        void convertsEachDomainSymptomToAssociatedSymptomEntity() {
            CycleSymptomEntry symptom = CycleSymptomEntry.builder()
                    .symptom(CycleSymptom.CRAMPS)
                    .severity(SymptomSeverity.MODERATE)
                    .build();

            CycleDayEntry entry = CycleDayEntry.create(
                    cycleId, patientId, LocalDate.of(2026, 6, 1), FlowIntensity.MODERATE, null,
                    List.of(symptom));

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            adapter.save(entry);

            ArgumentCaptor<CycleDayEntryEntity> captor = ArgumentCaptor.forClass(CycleDayEntryEntity.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getSymptoms()).hasSize(1);
            assertThat(captor.getValue().getSymptoms().get(0).getSymptomCode()).isEqualTo("CRAMPS");
            assertThat(captor.getValue().getSymptoms().get(0).getSeverity()).isEqualTo("MODERATE");
            assertThat(captor.getValue().getSymptoms().get(0).getDayEntry()).isSameAs(captor.getValue());
        }

        @Test
        @DisplayName("retorna el dominio reconstruido a partir de la entidad guardada")
        void returnsDomainReconstructedFromSavedEntity() {
            CycleDayEntry entry = CycleDayEntry.create(
                    cycleId, patientId, LocalDate.of(2026, 6, 1), FlowIntensity.HEAVY, "nota", null);

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            CycleDayEntry result = adapter.save(entry);

            assertThat(result.getFlowIntensity()).isEqualTo(FlowIntensity.HEAVY);
            assertThat(result.getNotes()).isEqualTo("nota");
        }
    }

    @Nested
    @DisplayName("findByCycleIdAndDate")
    class FindByCycleIdAndDate {

        @Test
        @DisplayName("retorna la entrada con sus síntomas reconstruidos cuando existe")
        void returnsEntryWithReconstructedSymptomsWhenExists() {
            CycleDayEntryEntity entity = entityWithOneSymptom();
            when(repository.findByCycleIdAndEntryDate(cycleId, LocalDate.of(2026, 6, 1)))
                    .thenReturn(Optional.of(entity));

            Optional<CycleDayEntry> result = adapter.findByCycleIdAndDate(cycleId, LocalDate.of(2026, 6, 1));

            assertThat(result).isPresent();
            assertThat(result.get().getSymptoms()).hasSize(1);
            assertThat(result.get().getSymptoms().get(0).getSymptom()).isEqualTo(CycleSymptom.BLOATING);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando no existe registro para esa fecha")
        void returnsEmptyWhenNoEntryForDate() {
            when(repository.findByCycleIdAndEntryDate(cycleId, LocalDate.of(2026, 6, 1)))
                    .thenReturn(Optional.empty());

            Optional<CycleDayEntry> result = adapter.findByCycleIdAndDate(cycleId, LocalDate.of(2026, 6, 1));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCycleId")
    class FindByCycleId {

        @Test
        @DisplayName("retorna todas las entradas del ciclo con sus síntomas reconstruidos")
        void returnsAllEntriesWithReconstructedSymptoms() {
            when(repository.findByCycleIdOrderByEntryDate(cycleId))
                    .thenReturn(List.of(entityWithOneSymptom()));

            List<CycleDayEntry> result = adapter.findByCycleId(cycleId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSymptoms()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findByPatientIdAndDateRange")
    class FindByPatientIdAndDateRange {

        @Test
        @DisplayName("retorna las entradas del rango con sus síntomas reconstruidos")
        void returnsEntriesInRangeWithReconstructedSymptoms() {
            LocalDate from = LocalDate.of(2026, 6, 1);
            LocalDate to = LocalDate.of(2026, 6, 7);
            when(repository.findByPatientIdAndEntryDateBetweenOrderByEntryDate(patientId, from, to))
                    .thenReturn(List.of(entityWithOneSymptom()));

            List<CycleDayEntry> result = adapter.findByPatientIdAndDateRange(patientId, from, to);

            assertThat(result).hasSize(1);
        }
    }


    private CycleDayEntryEntity entityWithOneSymptom() {
        CycleDaySymptomEntity symptomEntity = CycleDaySymptomEntity.builder()
                .id(UUID.randomUUID())
                .symptomCode("BLOATING")
                .severity("MILD")
                .build();

        return CycleDayEntryEntity.builder()
                .id(UUID.randomUUID())
                .cycleId(cycleId)
                .patientId(patientId)
                .entryDate(LocalDate.of(2026, 6, 1))
                .flowIntensity("MODERATE")
                .symptoms(List.of(symptomEntity))
                .build();
    }
}
