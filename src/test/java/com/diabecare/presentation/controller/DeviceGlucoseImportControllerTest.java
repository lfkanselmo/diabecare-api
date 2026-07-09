package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.ImportGlucoseReadingsUseCase;
import com.diabecare.domain.exception.InvalidDeviceApiKeyException;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.mapper.GlucoseReadingPresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceGlucoseImportController")
class DeviceGlucoseImportControllerTest {

    @Mock
    private ImportGlucoseReadingsUseCase importGlucoseReadingsUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        GlucoseReadingPresentationMapper mapper = Mappers.getMapper(GlucoseReadingPresentationMapper.class);
        DeviceGlucoseImportController controller =
                new DeviceGlucoseImportController(importGlucoseReadingsUseCase, mapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("retorna 201 con las lecturas importadas cuando la key es válida")
    void returns201WithImportedReadingsWhenKeyIsValid() throws Exception {
        GlucoseReading reading = GlucoseReading.create(
                UUID.randomUUID(), new BigDecimal("120"), GlucoseUnit.MG_DL,
                ReadingType.RANDOM, LocalDateTime.now().minusMinutes(5), null, "Dexcom G6");
        when(importGlucoseReadingsUseCase.execute(any())).thenReturn(List.of(reading));

        String body = """
                {"readings":[{"value":120,"unit":"MG_DL","readingType":"RANDOM","measuredAt":"2026-01-01T10:00:00"}]}
                """;

        mockMvc.perform(post("/api/v1/glucose/import")
                        .header("X-Device-Api-Key", "dbc_rawkey123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].deviceSource").value("Dexcom G6"));
    }

    @Test
    @DisplayName("retorna 401 cuando la key es inválida")
    void returns401WhenKeyIsInvalid() throws Exception {
        when(importGlucoseReadingsUseCase.execute(any()))
                .thenThrow(new InvalidDeviceApiKeyException("API key de dispositivo inválida."));

        String body = """
                {"readings":[{"value":120,"unit":"MG_DL","readingType":"RANDOM","measuredAt":"2026-01-01T10:00:00"}]}
                """;

        mockMvc.perform(post("/api/v1/glucose/import")
                        .header("X-Device-Api-Key", "dbc_wrongkey")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("retorna 400 cuando el batch de lecturas está vacío")
    void returns400WhenReadingsBatchIsEmpty() throws Exception {
        String body = """
                {"readings":[]}
                """;

        mockMvc.perform(post("/api/v1/glucose/import")
                        .header("X-Device-Api-Key", "dbc_rawkey123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(importGlucoseReadingsUseCase);
    }
}
