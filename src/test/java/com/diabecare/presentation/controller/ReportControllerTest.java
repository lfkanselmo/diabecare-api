package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GenerateMedicalReportUseCase;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportController")
class ReportControllerTest {

    @Mock private GenerateMedicalReportUseCase generateMedicalReportUseCase;
    @Mock private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReportController controller = new ReportController(generateMedicalReportUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Nested @DisplayName("GET /api/v1/reports/{patientId}/medical")
    class GenerateMedicalReport {
        @Test @DisplayName("retorna 200 con Content-Type PDF y filename correcto en Content-Disposition")
        void returns200WithPdfContentTypeAndCorrectFilename() throws Exception {
            byte[] fakePdf = new byte[]{0x25, 0x50, 0x44, 0x46};
            when(generateMedicalReportUseCase.generate(any())).thenReturn(fakePdf);

            mockMvc.perform(get("/api/v1/reports/{patientId}/medical", patientId)
                            .param("from", "2026-06-01").param("to", "2026-06-30"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=\"DiabeCare_Reporte_01062026_30062026.pdf\""))
                    .andExpect(content().contentType("application/pdf"));

            verify(currentUserResolver).verifyOwnsPatient(eq(patientId), any());
        }

        @Test @DisplayName("retorna 403 cuando el paciente no pertenece al usuario autenticado")
        void returns403WhenPatientDoesNotBelongToAuthenticatedUser() throws Exception {
            doThrow(new UnauthorizedResourceAccessException("sin permiso"))
                    .when(currentUserResolver).verifyOwnsPatient(eq(patientId), any());

            mockMvc.perform(get("/api/v1/reports/{patientId}/medical", patientId)
                            .param("from", "2026-06-01").param("to", "2026-06-30"))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(generateMedicalReportUseCase);
        }
    }
}