package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.infrastructure.persistence.entity.MedicationEntity;
import com.diabecare.infrastructure.persistence.mapper.MedicationPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.MedicationJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicationPersistenceAdapter")
class MedicationPersistenceAdapterTest {

    @Mock
    private MedicationJpaRepository repository;

    private MedicationPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new MedicationPersistenceAdapter(repository, new MedicationPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persiste el medicamento y retorna el dominio reconstruido")
        void persistsMedicationAndReturnsReconstructedDomain() {
            Medication medication = Medication.create(
                    patientId, "Metformina", MedicationType.ORAL, BigDecimal.valueOf(500),
                    DoseUnit.MG, MedicationFrequency.TWICE_DAILY, LocalDate.of(2026, 1, 1), null);

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Medication result = adapter.save(medication);

            assertThat(result.getName()).isEqualTo("Metformina");
            verify(repository).save(any(MedicationEntity.class));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna el medicamento convertido a dominio cuando existe")
        void returnsMedicationConvertedToDomainWhenExists() {
            MedicationEntity entity = validEntity();
            when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

            Optional<Medication> result = adapter.findById(entity.getId());

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findActiveByPatientId")
    class FindActiveByPatientId {

        @Test
        @DisplayName("retorna solo los medicamentos activos convertidos a dominio")
        void returnsOnlyActiveMedicationsConvertedToDomain() {
            when(repository.findByPatientIdAndActiveTrue(patientId)).thenReturn(List.of(validEntity()));

            List<Medication> result = adapter.findActiveByPatientId(patientId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findAllByPatientId")
    class FindAllByPatientId {

        @Test
        @DisplayName("retorna todos los medicamentos del paciente convertidos a dominio")
        void returnsAllPatientMedicationsConvertedToDomain() {
            when(repository.findByPatientId(patientId)).thenReturn(List.of(validEntity()));

            List<Medication> result = adapter.findAllByPatientId(patientId);

            assertThat(result).hasSize(1);
        }
    }


    private MedicationEntity validEntity() {
        return MedicationEntity.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .name("Metformina")
                .type("ORAL")
                .dose(BigDecimal.valueOf(500))
                .doseUnit("MG")
                .frequency("TWICE_DAILY")
                .startDate(LocalDate.of(2026, 1, 1))
                .active(true)
                .build();
    }
}
