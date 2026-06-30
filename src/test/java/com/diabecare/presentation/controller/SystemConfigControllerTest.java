package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GetSystemConfigUseCase;
import com.diabecare.domain.model.SystemConfig;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemConfigController")
class SystemConfigControllerTest {

    @Mock private GetSystemConfigUseCase getSystemConfigUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SystemConfigController controller = new SystemConfigController(getSystemConfigUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Nested @DisplayName("GET /api/v1/system-config")
    class GetAll {
        @Test @DisplayName("retorna 200 con todas las configuraciones mapeadas correctamente")
        void returns200WithAllConfigsMappedCorrectly() throws Exception {
            SystemConfig config = SystemConfig.builder()
                    .key("alerts.glucose.low.threshold").value("70")
                    .dataType(SystemConfig.DataType.INTEGER).category(SystemConfig.Category.ALERTS)
                    .description("Umbral bajo de glucosa").updatedAt(LocalDateTime.of(2026, 6, 1, 0, 0))
                    .build();

            when(getSystemConfigUseCase.getAll()).thenReturn(List.of(config));

            mockMvc.perform(get("/api/v1/system-config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].key").value("alerts.glucose.low.threshold"))
                    .andExpect(jsonPath("$[0].dataType").value("INTEGER"))
                    .andExpect(jsonPath("$[0].category").value("ALERTS"));
        }

        @Test @DisplayName("retorna 200 con lista vacía cuando no hay configuraciones")
        void returns200WithEmptyListWhenNoConfigs() throws Exception {
            when(getSystemConfigUseCase.getAll()).thenReturn(List.of());
            mockMvc.perform(get("/api/v1/system-config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested @DisplayName("POST /api/v1/system-config/reload")
    class Reload {
        @Test @DisplayName("retorna 200 y delega la recarga al caso de uso")
        void returns200AndDelegatesReloadToUseCase() throws Exception {
            mockMvc.perform(post("/api/v1/system-config/reload"))
                    .andExpect(status().isOk());
            verify(getSystemConfigUseCase).reload();
        }
    }
}