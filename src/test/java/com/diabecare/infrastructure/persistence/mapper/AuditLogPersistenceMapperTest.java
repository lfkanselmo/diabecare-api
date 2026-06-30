package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.AuditLog;
import com.diabecare.infrastructure.persistence.entity.AuditLogEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuditLogPersistenceMapper")
class AuditLogPersistenceMapperTest {

    private final AuditLogPersistenceMapper mapper = new AuditLogPersistenceMapperImpl();

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo el enum Action a String")
        void mapsAllFieldsConvertingActionEnumToString() {
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .entityType("PATIENT")
                    .entityId(UUID.randomUUID())
                    .action(AuditLog.Action.UPDATE)
                    .fieldName("heightCm")
                    .oldValue("165")
                    .newValue("170")
                    .performedAt(LocalDateTime.now())
                    .build();

            AuditLogEntity entity = mapper.toEntity(auditLog);

            assertThat(entity.getPatientId()).isEqualTo(auditLog.getPatientId());
            assertThat(entity.getEntityType()).isEqualTo("PATIENT");
            assertThat(entity.getEntityId()).isEqualTo(auditLog.getEntityId());
            assertThat(entity.getAction()).isEqualTo("UPDATE");
            assertThat(entity.getFieldName()).isEqualTo("heightCm");
            assertThat(entity.getOldValue()).isEqualTo("165");
            assertThat(entity.getNewValue()).isEqualTo("170");
        }

        @Test
        @DisplayName("ignora el id, dejando que la base de datos lo genere")
        void ignoresId() {
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .entityType("PATIENT")
                    .action(AuditLog.Action.CREATE)
                    .performedAt(LocalDateTime.now())
                    .build();

            AuditLogEntity entity = mapper.toEntity(auditLog);

            assertThat(entity.getId()).isNull();
        }

        @Test
        @DisplayName("ignora performedAt, dejando que @PrePersist lo establezca")
        void ignoresPerformedAt() {
            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .entityType("PATIENT")
                    .action(AuditLog.Action.DELETE)
                    .performedAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                    .build();

            AuditLogEntity entity = mapper.toEntity(auditLog);

            assertThat(entity.getPerformedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("mapea todos los campos correctamente, convirtiendo el String a enum Action")
        void mapsAllFieldsConvertingStringToActionEnum() {
            AuditLogEntity entity = AuditLogEntity.builder()
                    .id(UUID.randomUUID())
                    .patientId(UUID.randomUUID())
                    .entityType("MEDICATION")
                    .entityId(UUID.randomUUID())
                    .action("CREATE")
                    .performedAt(LocalDateTime.now())
                    .build();

            AuditLog auditLog = mapper.toDomain(entity);

            assertThat(auditLog.getId()).isEqualTo(entity.getId());
            assertThat(auditLog.getEntityType()).isEqualTo("MEDICATION");
            assertThat(auditLog.getAction()).isEqualTo(AuditLog.Action.CREATE);
        }
    }
}