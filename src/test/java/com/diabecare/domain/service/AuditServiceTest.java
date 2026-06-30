package com.diabecare.domain.service;

import com.diabecare.domain.model.AuditLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AuditService")
class AuditServiceTest {

    private final AuditService service = new AuditService();
    private final UUID patientId = UUID.randomUUID();
    private final UUID entityId = UUID.randomUUID();

    @Nested
    @DisplayName("buildCreateLog")
    class BuildCreateLog {

        @Test
        @DisplayName("construye un log con acción CREATE y los campos correctos")
        void buildsCreateLog() {
            AuditLog log = service.buildCreateLog(patientId, "GlucoseReading", entityId);

            assertThat(log.getPatientId()).isEqualTo(patientId);
            assertThat(log.getEntityType()).isEqualTo("GlucoseReading");
            assertThat(log.getEntityId()).isEqualTo(entityId);
            assertThat(log.getAction()).isEqualTo(AuditLog.Action.CREATE);
            assertThat(log.getFieldName()).isNull();
            assertThat(log.getOldValue()).isNull();
            assertThat(log.getNewValue()).isNull();
        }
    }

    @Nested
    @DisplayName("buildUpdateLog")
    class BuildUpdateLog {

        @Test
        @DisplayName("construye un log con acción UPDATE y los valores antiguo/nuevo")
        void buildsUpdateLog() {
            AuditLog log = service.buildUpdateLog(
                    patientId, "Patient", entityId, "targetGlucoseMax", "180", "160");

            assertThat(log.getAction()).isEqualTo(AuditLog.Action.UPDATE);
            assertThat(log.getFieldName()).isEqualTo("targetGlucoseMax");
            assertThat(log.getOldValue()).isEqualTo("180");
            assertThat(log.getNewValue()).isEqualTo("160");
        }
    }

    @Nested
    @DisplayName("buildDeleteLog")
    class BuildDeleteLog {

        @Test
        @DisplayName("construye un log con acción DELETE y los campos correctos")
        void buildsDeleteLog() {
            AuditLog log = service.buildDeleteLog(patientId, "MealEntry", entityId);

            assertThat(log.getPatientId()).isEqualTo(patientId);
            assertThat(log.getEntityType()).isEqualTo("MealEntry");
            assertThat(log.getEntityId()).isEqualTo(entityId);
            assertThat(log.getAction()).isEqualTo(AuditLog.Action.DELETE);
        }
    }
}