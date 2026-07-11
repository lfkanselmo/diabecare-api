package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetHba1cTrendUseCase;
import com.diabecare.application.port.in.GetVitalSignsUseCase;
import com.diabecare.application.port.in.RegisterVitalSignUseCase;
import com.diabecare.application.port.in.SyncVitalSignsUseCase;
import com.diabecare.domain.model.VitalSign;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.mapper.VitalSignPresentationMapperImpl;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VitalSignController")
class VitalSignControllerTest {

    @Mock private RegisterVitalSignUseCase registerVitalSignUseCase;
    @Mock private GetVitalSignsUseCase getVitalSignsUseCase;
    @Mock private GetHba1cTrendUseCase getHba1cTrendUseCase;
    @Mock private SyncVitalSignsUseCase syncVitalSignsUseCase;
    @Mock private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        VitalSignController controller = new VitalSignController(
                registerVitalSignUseCase, getVitalSignsUseCase,
                new VitalSignPresentationMapperImpl(),
                getHba1cTrendUseCase, syncVitalSignsUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Nested @DisplayName("POST /api/v1/vitals/{patientId}")
    class Register {
        @Test @DisplayName("retorna 201 con el signo vital registrado correctamente mapeado")
        void returns201WithRegisteredVitalSignMappedCorrectly() throws Exception {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID()).patientId(patientId)
                    .weightKg(BigDecimal.valueOf(70)).heightCm(BigDecimal.valueOf(170))
                    .measuredAt(LocalDateTime.now().minusMinutes(5)).build();

            when(registerVitalSignUseCase.execute(any())).thenReturn(vitalSign);

            mockMvc.perform(post("/api/v1/vitals/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"weightKg\":70,\"heightCm\":170,\"measuredAt\":\"2026-06-15T08:00:00\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.weightKg").value(70))
                    .andExpect(jsonPath("$.bmiCategory").value("NORMAL"));
        }
    }

    @Nested @DisplayName("GET /api/v1/vitals/{patientId}")
    class GetAll {
        @Test @DisplayName("retorna 200 con la página de signos vitales mapeada correctamente")
        void returns200WithAllVitalSignsMappedCorrectly() throws Exception {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID()).patientId(patientId)
                    .weightKg(BigDecimal.valueOf(68)).measuredAt(LocalDateTime.now().minusDays(1)).build();
            var pageable = PageRequest.of(0, 20);

            when(getVitalSignsUseCase.getByPatientId(patientId, pageable))
                    .thenReturn(new PageImpl<>(List.of(vitalSign), pageable, 1));

            mockMvc.perform(get("/api/v1/vitals/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].weightKg").value(68))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested @DisplayName("GET /api/v1/vitals/{patientId}/latest")
    class GetLatest {
        @Test @DisplayName("retorna 200 con el último signo vital cuando existe")
        void returns200WithLatestVitalSignWhenExists() throws Exception {
            VitalSign vitalSign = VitalSign.builder()
                    .vitalId(UUID.randomUUID()).patientId(patientId)
                    .weightKg(BigDecimal.valueOf(70)).measuredAt(LocalDateTime.now().minusMinutes(5)).build();

            when(getVitalSignsUseCase.getLatest(patientId)).thenReturn(Optional.of(vitalSign));

            mockMvc.perform(get("/api/v1/vitals/{patientId}/latest", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.weightKg").value(70));
        }

        @Test @DisplayName("retorna 204 sin cuerpo cuando el paciente no tiene signos vitales")
        void returns204WithoutBodyWhenNoVitalSigns() throws Exception {
            when(getVitalSignsUseCase.getLatest(patientId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/vitals/{patientId}/latest", patientId))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested @DisplayName("GET /api/v1/vitals/{patientId}/hba1c-trend")
    class GetHba1cTrend {
        @Test @DisplayName("retorna 200 con la tendencia HbA1c mapeada correctamente")
        void returns200WithHba1cTrendMappedCorrectly() throws Exception {
            var monthly = new GetHba1cTrendUseCase.MonthlyHba1c(
                    "2026-05", BigDecimal.valueOf(6.5), BigDecimal.valueOf(120), 45);
            when(getHba1cTrendUseCase.getTrend(patientId, 6)).thenReturn(List.of(monthly));

            mockMvc.perform(get("/api/v1/vitals/{patientId}/hba1c-trend", patientId).param("months", "6"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].month").value("2026-05"))
                    .andExpect(jsonPath("$[0].estimatedHba1c").value(6.5))
                    .andExpect(jsonPath("$[0].totalReadings").value(45));
        }

        @Test @DisplayName("usa 6 meses como valor por defecto cuando no se especifica el parámetro")
        void usesDefaultSixMonthsWhenParameterNotSpecified() throws Exception {
            when(getHba1cTrendUseCase.getTrend(patientId, 6)).thenReturn(List.of());
            mockMvc.perform(get("/api/v1/vitals/{patientId}/hba1c-trend", patientId))
                    .andExpect(status().isOk());
            verify(getHba1cTrendUseCase).getTrend(patientId, 6);
        }
    }
}