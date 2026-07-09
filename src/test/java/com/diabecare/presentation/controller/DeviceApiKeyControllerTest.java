package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.GenerateDeviceApiKeyUseCase;
import com.diabecare.application.port.in.ListDeviceApiKeysUseCase;
import com.diabecare.application.port.in.RevokeDeviceApiKeyUseCase;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.domain.model.DeviceApiKey;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeviceApiKeyController")
class DeviceApiKeyControllerTest {

    @Mock
    private GenerateDeviceApiKeyUseCase generateDeviceApiKeyUseCase;
    @Mock
    private ListDeviceApiKeysUseCase listDeviceApiKeysUseCase;
    @Mock
    private RevokeDeviceApiKeyUseCase revokeDeviceApiKeyUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        DeviceApiKeyController controller = new DeviceApiKeyController(
                generateDeviceApiKeyUseCase, listDeviceApiKeysUseCase, revokeDeviceApiKeyUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/device-keys/{patientId}")
    class Generate {

        @Test
        @DisplayName("retorna 201 con la key cruda y el label")
        void returns201WithRawKeyAndLabel() throws Exception {
            UUID keyId = UUID.randomUUID();
            when(generateDeviceApiKeyUseCase.execute(any())).thenReturn(
                    new GenerateDeviceApiKeyUseCase.Result(keyId, "dbc_rawkey123", "Dexcom G6", LocalDateTime.now()));

            String body = """
                    {"label":"Dexcom G6"}
                    """;

            mockMvc.perform(post("/api/v1/device-keys/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rawKey").value("dbc_rawkey123"))
                    .andExpect(jsonPath("$.label").value("Dexcom G6"));

            verify(currentUserResolver).verifyOwnsPatient(eq(patientId), any());
        }

        @Test
        @DisplayName("retorna 400 cuando el label está en blanco")
        void returns400WhenLabelIsBlank() throws Exception {
            String body = """
                    {"label":""}
                    """;

            mockMvc.perform(post("/api/v1/device-keys/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(generateDeviceApiKeyUseCase);
        }

        @Test
        @DisplayName("retorna 403 cuando el paciente no pertenece al usuario autenticado")
        void returns403WhenPatientDoesNotBelongToAuthenticatedUser() throws Exception {
            doThrow(new UnauthorizedResourceAccessException("sin permiso"))
                    .when(currentUserResolver).verifyOwnsPatient(eq(patientId), any());

            String body = """
                    {"label":"Dexcom G6"}
                    """;

            mockMvc.perform(post("/api/v1/device-keys/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(generateDeviceApiKeyUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/device-keys/{patientId}")
    class List_ {

        @Test
        @DisplayName("retorna 200 con las keys mapeadas correctamente, sin exponer el hash")
        void returns200WithKeysMappedCorrectlyWithoutExposingHash() throws Exception {
            DeviceApiKey key = DeviceApiKey.builder()
                    .id(UUID.randomUUID()).label("Dexcom G6")
                    .createdAt(LocalDateTime.now()).build();
            when(listDeviceApiKeysUseCase.execute(patientId)).thenReturn(List.of(key));

            mockMvc.perform(get("/api/v1/device-keys/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].label").value("Dexcom G6"))
                    .andExpect(jsonPath("$[0].revoked").value(false))
                    .andExpect(jsonPath("$[0].keyHash").doesNotExist());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/device-keys/{patientId}/{keyId}")
    class Revoke {

        @Test
        @DisplayName("retorna 204 al revocar correctamente")
        void returns204OnSuccessfulRevoke() throws Exception {
            UUID keyId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/device-keys/{patientId}/{keyId}", patientId, keyId))
                    .andExpect(status().isNoContent());

            verify(revokeDeviceApiKeyUseCase).execute(
                    new RevokeDeviceApiKeyUseCase.Command(patientId, keyId));
        }
    }
}
