package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.*;
import com.diabecare.domain.model.BiologicalSex;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.mapper.PatientPresentationMapperImpl;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Mock
    private LoginUseCase loginUseCase;
    @Mock
    private RegisterUseCase registerUseCase;
    @Mock
    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    @Mock
    private LogoutCurrentSessionUseCase logoutCurrentSessionUseCase;
    @Mock
    private LogoutAllSessionsUseCase logoutAllSessionsUseCase;
    @Mock
    private GetActiveSessionsUseCase getActiveSessionsUseCase;
    @Mock
    private GetPatientUseCase getPatientUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(
                loginUseCase, registerUseCase, refreshAccessTokenUseCase,
                logoutCurrentSessionUseCase, logoutAllSessionsUseCase, getActiveSessionsUseCase,
                getPatientUseCase, new PatientPresentationMapperImpl(), currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/register")
    class Register {

        @Test
        @DisplayName("retorna 201 con el AuthResponse completo en un registro exitoso")
        void returns201WithCompleteAuthResponseOnSuccessfulRegistration() throws Exception {
            var result = new RegisterUseCase.Result(
                    "access-token", 3600L, "refresh-token", 604800000L,
                    UUID.randomUUID().toString(), userId.toString());

            when(registerUseCase.execute(any())).thenReturn(result);
            when(getPatientUseCase.getByUserId(userId))
                    .thenReturn(new GetPatientUseCase.Result(validPatient()));

            String body = """
                    {"email":"ana@example.com","password":"password123","fullName":"Ana García",
                     "dateOfBirth":"1990-05-10","diabetesType":"TYPE_1","diagnosisDate":"2010-01-01",
                     "heightCm":"165","biologicalSex":"FEMALE","termsAccepted":true}
                    """;

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.patient.fullName").value("Ana García"));
        }

        @Test
        @DisplayName("retorna 400 cuando el email tiene formato inválido")
        void returns400WhenEmailHasInvalidFormat() throws Exception {
            String body = """
                    {"email":"no-es-un-email","password":"password123","fullName":"Ana García",
                     "dateOfBirth":"1990-05-10","diabetesType":"TYPE_1","diagnosisDate":"2010-01-01",
                     "heightCm":"165","biologicalSex":"FEMALE","termsAccepted":true}
                    """;

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(registerUseCase);
        }

        @Test
        @DisplayName("retorna 400 cuando el password tiene menos de 8 caracteres")
        void returns400WhenPasswordTooShort() throws Exception {
            String body = """
                    {"email":"ana@example.com","password":"corta","fullName":"Ana García",
                     "dateOfBirth":"1990-05-10","diabetesType":"TYPE_1","diagnosisDate":"2010-01-01",
                     "heightCm":"165","biologicalSex":"FEMALE","termsAccepted":true}
                    """;

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("retorna 400 cuando no se acepta la politica de tratamiento de datos")
        void returns400WhenTermsNotAccepted() throws Exception {
            String body = """
                    {"email":"ana@example.com","password":"password123","fullName":"Ana García",
                     "dateOfBirth":"1990-05-10","diabetesType":"TYPE_1","diagnosisDate":"2010-01-01",
                     "heightCm":"165","biologicalSex":"FEMALE","termsAccepted":false}
                    """;

            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(registerUseCase);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {

        @Test
        @DisplayName("retorna 200 con el AuthResponse en un login exitoso")
        void returns200WithAuthResponseOnSuccessfulLogin() throws Exception {
            var result = new LoginUseCase.Result(
                    "access-token", 3600L, "refresh-token", 604800000L,
                    UUID.randomUUID().toString(), userId.toString());

            when(loginUseCase.execute(any())).thenReturn(result);
            when(getPatientUseCase.getByUserId(userId))
                    .thenReturn(new GetPatientUseCase.Result(validPatient()));

            String body = """
                    {"email":"ana@example.com","password":"password123"}
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"));
        }

        @Test
        @DisplayName("retorna 401 cuando las credenciales son inválidas")
        void returns401WhenCredentialsInvalid() throws Exception {
            when(loginUseCase.execute(any()))
                    .thenThrow(new BadCredentialsException("credenciales inválidas"));

            String body = """
                    {"email":"ana@example.com","password":"wrongpassword"}
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }

        @Test
        @DisplayName("retorna 400 cuando el password está vacío")
        void returns400WhenPasswordBlank() throws Exception {
            String body = """
                    {"email":"ana@example.com","password":""}
                    """;

            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("retorna 200 con los nuevos tokens")
        void returns200WithNewTokens() throws Exception {
            var result = new RefreshAccessTokenUseCase.Result(
                    "new-access-token", 3600L, "new-refresh-token", 604800000L);
            when(refreshAccessTokenUseCase.execute(any())).thenReturn(result);

            String body = """
                    {"refreshToken":"valid-refresh-token"}
                    """;

            mockMvc.perform(post("/api/v1/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("new-access-token"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {

        @Test
        @DisplayName("retorna 204 al cerrar sesión en el dispositivo actual")
        void returns204OnCurrentDeviceLogout() throws Exception {
            String body = """
                    {"refreshToken":"valid-refresh-token"}
                    """;

            mockMvc.perform(post("/api/v1/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNoContent());

            verify(logoutCurrentSessionUseCase).execute(any());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout-all")
    class LogoutAll {

        @Test
        @DisplayName("retorna 204 al cerrar todas las sesiones del usuario propio")
        void returns204WhenLoggingOutAllSessionsOfOwnUser() throws Exception {
            String body = "{\"userId\":\"" + userId + "\"}";

            mockMvc.perform(post("/api/v1/auth/logout-all")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNoContent());

            verify(currentUserResolver).verifyIsCurrentUser(eq(userId), any());
            verify(logoutAllSessionsUseCase).execute(any());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/auth/sessions/{userId}")
    class GetActiveSessions {

        @Test
        @DisplayName("retorna 200 con la lista de sesiones activas")
        void returns200WithActiveSessionsList() throws Exception {
            var session = new com.diabecare.application.port.out.RefreshTokenPort.ActiveSession(
                    UUID.randomUUID(), "iPhone", LocalDateTime.now(), LocalDateTime.now().minusDays(1));
            when(getActiveSessionsUseCase.execute(userId)).thenReturn(List.of(session));

            mockMvc.perform(get("/api/v1/auth/sessions/{userId}", userId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].deviceLabel").value("iPhone"));
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private Patient validPatient() {
        Patient patient = Patient.create(
                userId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
        patient.updateBiologicalSex(BiologicalSex.FEMALE);
        return patient;
    }
}