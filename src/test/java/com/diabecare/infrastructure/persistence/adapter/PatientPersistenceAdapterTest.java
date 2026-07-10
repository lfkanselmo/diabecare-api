package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import com.diabecare.infrastructure.persistence.entity.PatientEntity;
import com.diabecare.infrastructure.persistence.mapper.PatientPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.PatientJpaRepository;
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
@DisplayName("PatientPersistenceAdapter")
class PatientPersistenceAdapterTest {

    @Mock
    private PatientJpaRepository repository;

    private PatientPersistenceAdapter adapter;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new PatientPersistenceAdapter(repository, new PatientPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persiste el paciente y retorna el dominio reconstruido")
        void persistsPatientAndReturnsReconstructedDomain() {
            Patient patient = Patient.create(
                    userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));

            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            Patient result = adapter.save(patient);

            assertThat(result.getFullName()).isEqualTo("Ana García");
            verify(repository).save(any(PatientEntity.class));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna el paciente convertido a dominio cuando existe")
        void returnsPatientConvertedToDomainWhenExists() {
            PatientEntity entity = validEntity();
            when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

            Optional<Patient> result = adapter.findById(entity.getId());

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("findByUserId")
    class FindByUserId {

        @Test
        @DisplayName("retorna el paciente convertido a dominio cuando existe para ese usuario")
        void returnsPatientConvertedToDomainForUser() {
            PatientEntity entity = validEntity();
            when(repository.findByUserId(userId)).thenReturn(Optional.of(entity));

            Optional<Patient> result = adapter.findByUserId(userId);

            assertThat(result).isPresent();
        }
    }

    @Nested
    @DisplayName("existsByUserId")
    class ExistsByUserId {

        @Test
        @DisplayName("delega la verificación de existencia al repositorio")
        void delegatesExistenceCheckToRepository() {
            when(repository.existsByUserId(userId)).thenReturn(true);

            boolean result = adapter.existsByUserId(userId);

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("retorna todos los pacientes convertidos a dominio")
        void returnsAllPatientsConvertedToDomain() {
            when(repository.findAll()).thenReturn(List.of(validEntity()));

            List<Patient> result = adapter.findAll();

            assertThat(result).hasSize(1);
        }
    }


    private PatientEntity validEntity() {
        return PatientEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .fullName("Ana García")
                .dateOfBirth(LocalDate.of(1990, 5, 10))
                .diabetesType("TYPE_1")
                .diagnosisDate(LocalDate.of(2010, 1, 1))
                .heightCm(BigDecimal.valueOf(165))
                .targetGlucoseMin(BigDecimal.valueOf(70))
                .targetGlucoseMax(BigDecimal.valueOf(180))
                .activityLevel("SEDENTARY")
                .preferredGlucoseUnit("MG_DL")
                .build();
    }
}
