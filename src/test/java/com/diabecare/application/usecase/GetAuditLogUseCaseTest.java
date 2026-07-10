package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadAuditLogPort;
import com.diabecare.domain.model.AuditLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAuditLogUseCaseImpl")
class GetAuditLogUseCaseTest {

    @Mock
    private LoadAuditLogPort loadAuditLogPort;

    @InjectMocks
    private GetAuditLogUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("getByPatient")
    class GetByPatient {

        @Test
        @DisplayName("retorna todos los logs de auditoría del paciente")
        void returnsAllAuditLogsForPatient() {
            AuditLog log = auditLog();
            when(loadAuditLogPort.findByPatientId(patientId)).thenReturn(List.of(log));

            List<AuditLog> result = useCase.getByPatient(patientId);

            assertThat(result).containsExactly(log);
        }
    }

    @Nested
    @DisplayName("getByPatientAndEntity")
    class GetByPatientAndEntity {

        @Test
        @DisplayName("retorna los logs filtrados por tipo de entidad")
        void returnsLogsFilteredByEntityType() {
            AuditLog log = auditLog();
            when(loadAuditLogPort.findByPatientIdAndEntityType(patientId, "PATIENT"))
                    .thenReturn(List.of(log));

            List<AuditLog> result = useCase.getByPatientAndEntity(patientId, "PATIENT");

            assertThat(result).containsExactly(log);
        }
    }


    private AuditLog auditLog() {
        return AuditLog.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .entityType("PATIENT")
                .entityId(patientId)
                .action(AuditLog.Action.UPDATE)
                .performedAt(LocalDateTime.now())
                .build();
    }
}
