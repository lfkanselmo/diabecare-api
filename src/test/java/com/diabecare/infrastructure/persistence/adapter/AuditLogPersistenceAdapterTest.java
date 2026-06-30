package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.AuditLog;
import com.diabecare.infrastructure.persistence.entity.AuditLogEntity;
import com.diabecare.infrastructure.persistence.mapper.AuditLogPersistenceMapperImpl;
import com.diabecare.infrastructure.persistence.repository.AuditLogJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditLogPersistenceAdapter")
class AuditLogPersistenceAdapterTest {

    @Mock
    private AuditLogJpaRepository repository;

    private AuditLogPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new AuditLogPersistenceAdapter(repository, new AuditLogPersistenceMapperImpl());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persiste el log convertido a entidad a través del repositorio")
        void persistsLogConvertedToEntity() {
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .entityType("PATIENT")
                    .entityId(patientId)
                    .action(AuditLog.Action.UPDATE)
                    .fieldName("heightCm")
                    .oldValue("165")
                    .newValue("170")
                    .performedAt(LocalDateTime.now())
                    .build();

            adapter.save(auditLog);

            ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getEntityType()).isEqualTo("PATIENT");
            assertThat(captor.getValue().getAction()).isEqualTo("UPDATE");
            assertThat(captor.getValue().getFieldName()).isEqualTo("heightCm");
        }
    }

    @Nested
    @DisplayName("findByPatientId")
    class FindByPatientId {

        @Test
        @DisplayName("retorna los logs del paciente convertidos a dominio")
        void returnsPatientLogsConvertedToDomain() {
            AuditLogEntity entity = AuditLogEntity.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .entityType("MEDICATION")
                    .action("CREATE")
                    .performedAt(LocalDateTime.now())
                    .build();

            when(repository.findFirst200ByPatientIdOrderByPerformedAtDesc(patientId))
                    .thenReturn(List.of(entity));

            List<AuditLog> result = adapter.findByPatientId(patientId);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEntityType()).isEqualTo("MEDICATION");
            assertThat(result.get(0).getAction()).isEqualTo(AuditLog.Action.CREATE);
        }

        @Test
        @DisplayName("retorna lista vacía cuando el paciente no tiene logs")
        void returnsEmptyListWhenNoLogs() {
            when(repository.findFirst200ByPatientIdOrderByPerformedAtDesc(patientId))
                    .thenReturn(List.of());

            List<AuditLog> result = adapter.findByPatientId(patientId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPatientIdAndEntityType")
    class FindByPatientIdAndEntityType {

        @Test
        @DisplayName("retorna los logs filtrados por tipo de entidad convertidos a dominio")
        void returnsLogsFilteredByEntityType() {
            AuditLogEntity entity = AuditLogEntity.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .entityType("PATIENT")
                    .action("UPDATE")
                    .performedAt(LocalDateTime.now())
                    .build();

            when(repository.findFirst200ByPatientIdAndEntityTypeOrderByPerformedAtDesc(patientId, "PATIENT"))
                    .thenReturn(List.of(entity));

            List<AuditLog> result = adapter.findByPatientIdAndEntityType(patientId, "PATIENT");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEntityType()).isEqualTo("PATIENT");
        }
    }
}