package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetAuditLogUseCase;
import com.diabecare.domain.model.AuditLog;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuditController")
class AuditControllerTest {

    @Mock
    private GetAuditLogUseCase getAuditLogUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AuditController controller = new AuditController(getAuditLogUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/audit/{patientId}")
    class GetByPatient {

        @Test
        @DisplayName("retorna 200 con los logs mapeados correctamente")
        void returns200WithLogsMappedCorrectly() throws Exception {
            AuditLog log = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .patientId(patientId)
                    .entityType("PATIENT")
                    .action(AuditLog.Action.UPDATE)
                    .fieldName("heightCm")
                    .performedAt(LocalDateTime.now())
                    .build();

            when(getAuditLogUseCase.getByPatient(patientId)).thenReturn(List.of(log));

            mockMvc.perform(get("/api/v1/audit/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].entityType").value("PATIENT"))
                    .andExpect(jsonPath("$[0].action").value("UPDATE"))
                    .andExpect(jsonPath("$[0].fieldName").value("heightCm"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/audit/{patientId}/{entityType}")
    class GetByPatientAndEntity {

        @Test
        @DisplayName("convierte entityType a mayúsculas antes de delegar al caso de uso")
        void convertsEntityTypeToUppercaseBeforeDelegating() throws Exception {
            when(getAuditLogUseCase.getByPatientAndEntity(patientId, "MEDICATION")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/audit/{patientId}/{entityType}", patientId, "medication"))
                    .andExpect(status().isOk());

            verify(getAuditLogUseCase).getByPatientAndEntity(patientId, "MEDICATION");
        }
    }
}