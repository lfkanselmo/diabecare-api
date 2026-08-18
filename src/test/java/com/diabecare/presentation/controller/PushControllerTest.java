package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.RegisterMobileTokenUseCase;
import com.diabecare.application.port.in.SubscribeToPushUseCase;
import com.diabecare.application.port.in.UnregisterMobileTokenUseCase;
import com.diabecare.application.port.in.UnsubscribeFromPushUseCase;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.MobilePlatform;
import com.diabecare.domain.model.Patient;
import com.diabecare.infrastructure.config.DiabeCareProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PushController")
class PushControllerTest {

    @Mock private SubscribeToPushUseCase subscribeToPushUseCase;
    @Mock private UnsubscribeFromPushUseCase unsubscribeFromPushUseCase;
    @Mock private RegisterMobileTokenUseCase registerMobileTokenUseCase;
    @Mock private UnregisterMobileTokenUseCase unregisterMobileTokenUseCase;
    @Mock private LoadUserPort loadUserPort;
    @Mock private LoadPatientPort loadPatientPort;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DiabeCareProperties properties = new DiabeCareProperties(
                new DiabeCareProperties.Security(new String[]{"http://localhost:4200"}, 10),
                new DiabeCareProperties.Push("test-vapid-public-key", "test-vapid-private-key", "mailto:test@test.com", ""),
                new DiabeCareProperties.Mail("", "DiabeCare <onboarding@resend.dev>", "http://localhost:4200"));
        PushController controller = new PushController(
                subscribeToPushUseCase, unsubscribeFromPushUseCase,
                registerMobileTokenUseCase, unregisterMobileTokenUseCase,
                loadUserPort, loadPatientPort, properties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        UserDetails userDetails = new User(email, "hash", true, true, true, true, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
    }

    @Nested @DisplayName("GET /api/v1/push/vapid-public-key")
    class GetVapidPublicKey {
        @Test @DisplayName("retorna 200 con la clave pública VAPID configurada")
        void returns200WithConfiguredVapidPublicKey() throws Exception {
            mockMvc.perform(get("/api/v1/push/vapid-public-key"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.publicKey").value("test-vapid-public-key"));
        }
    }

    @Nested @DisplayName("POST /api/v1/push/subscribe")
    class Subscribe {
        @Test @DisplayName("retorna 200 y delega la suscripción cuando el paciente existe")
        void returns200AndDelegatesSubscriptionWhenPatientExists() throws Exception {
            UUID userId = UUID.randomUUID();
            Patient patient = Patient.create(userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
            UUID realPatientId = patient.getPatientId();

            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.of(patient));
            authenticateAs("ana@example.com");

            mockMvc.perform(post("/api/v1/push/subscribe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"endpoint\":\"https://push.example.com/sub\","
                                    + "\"p256dh\":\"key\",\"auth\":\"auth\"}"))
                    .andExpect(status().isOk());

            verify(subscribeToPushUseCase).execute(new SubscribeToPushUseCase.Command(
                    realPatientId, "https://push.example.com/sub", "key", "auth"));
        }

        @Test @DisplayName("retorna 200 sin suscribir cuando el usuario no tiene paciente asociado")
        void returns200WithoutSubscribingWhenUserHasNoPatient() throws Exception {
            UUID userId = UUID.randomUUID();
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.empty());
            authenticateAs("ana@example.com");

            mockMvc.perform(post("/api/v1/push/subscribe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"endpoint\":\"https://push.example.com/sub\","
                                    + "\"p256dh\":\"key\",\"auth\":\"auth\"}"))
                    .andExpect(status().isOk());

            verifyNoInteractions(subscribeToPushUseCase);
        }
    }

    @Nested @DisplayName("DELETE /api/v1/push/unsubscribe")
    class Unsubscribe {
        @Test @DisplayName("retorna 200 y delega la desuscripción al caso de uso")
        void returns200AndDelegatesUnsubscription() throws Exception {
            mockMvc.perform(delete("/api/v1/push/unsubscribe")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"endpoint\":\"https://push.example.com/sub\"}"))
                    .andExpect(status().isOk());

            verify(unsubscribeFromPushUseCase).execute(
                    new UnsubscribeFromPushUseCase.Command("https://push.example.com/sub"));
        }
    }

    @Nested @DisplayName("POST /api/v1/push/mobile-token")
    class RegisterMobileToken {
        @Test @DisplayName("retorna 200 y delega el registro cuando el paciente existe")
        void returns200AndDelegatesRegistrationWhenPatientExists() throws Exception {
            UUID userId = UUID.randomUUID();
            Patient patient = Patient.create(userId, "Ana García", LocalDate.of(1990, 5, 10),
                    DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
            UUID realPatientId = patient.getPatientId();

            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));
            when(loadPatientPort.findByUserId(userId)).thenReturn(Optional.of(patient));
            authenticateAs("ana@example.com");

            mockMvc.perform(post("/api/v1/push/mobile-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"deviceToken\":\"fcm-token-1\",\"platform\":\"ANDROID\"}"))
                    .andExpect(status().isOk());

            verify(registerMobileTokenUseCase).execute(new RegisterMobileTokenUseCase.Command(
                    realPatientId, "fcm-token-1", MobilePlatform.ANDROID));
        }

        @Test @DisplayName("retorna 400 cuando el device token está en blanco")
        void returns400WhenDeviceTokenIsBlank() throws Exception {
            mockMvc.perform(post("/api/v1/push/mobile-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"deviceToken\":\"\",\"platform\":\"ANDROID\"}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(registerMobileTokenUseCase);
        }
    }

    @Nested @DisplayName("DELETE /api/v1/push/mobile-token")
    class UnregisterMobileToken {
        @Test @DisplayName("retorna 200 y delega la eliminación al caso de uso")
        void returns200AndDelegatesUnregistration() throws Exception {
            mockMvc.perform(delete("/api/v1/push/mobile-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"deviceToken\":\"fcm-token-1\"}"))
                    .andExpect(status().isOk());

            verify(unregisterMobileTokenUseCase).execute(
                    new UnregisterMobileTokenUseCase.Command("fcm-token-1"));
        }
    }
}
