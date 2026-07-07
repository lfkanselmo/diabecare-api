package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.*;
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
@DisplayName("CaregiverController")
class CaregiverControllerTest {

    @Mock
    private CreateCaregiverInviteUseCase createCaregiverInviteUseCase;
    @Mock
    private RedeemCaregiverInviteUseCase redeemCaregiverInviteUseCase;
    @Mock
    private ListMyCaregiversUseCase listMyCaregiversUseCase;
    @Mock
    private ListPatientsICareForUseCase listPatientsICareForUseCase;
    @Mock
    private RevokeCaregiverAccessUseCase revokeCaregiverAccessUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        CaregiverController controller = new CaregiverController(
                createCaregiverInviteUseCase, redeemCaregiverInviteUseCase, listMyCaregiversUseCase,
                listPatientsICareForUseCase, revokeCaregiverAccessUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/caregivers/{patientId}/invites")
    class CreateInvite {

        @Test
        @DisplayName("retorna 201 con el código y la fecha de expiración")
        void returns201WithCodeAndExpiry() throws Exception {
            LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
            when(createCaregiverInviteUseCase.execute(patientId))
                    .thenReturn(new CreateCaregiverInviteUseCase.Result("ABCD-1234", expiresAt));

            mockMvc.perform(post("/api/v1/caregivers/{patientId}/invites", patientId))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.code").value("ABCD-1234"));

            verify(currentUserResolver).verifyOwnsPatient(eq(patientId), any());
        }

        @Test
        @DisplayName("retorna 403 cuando el paciente no pertenece al usuario autenticado")
        void returns403WhenPatientDoesNotBelongToAuthenticatedUser() throws Exception {
            doThrow(new UnauthorizedResourceAccessException("sin permiso"))
                    .when(currentUserResolver).verifyOwnsPatient(eq(patientId), any());

            mockMvc.perform(post("/api/v1/caregivers/{patientId}/invites", patientId))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(createCaregiverInviteUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/caregivers/{patientId}/links")
    class GetLinks {

        @Test
        @DisplayName("retorna 200 con los cuidadores mapeados correctamente")
        void returns200WithCaregiversMappedCorrectly() throws Exception {
            LocalDateTime linkedAt = LocalDateTime.now();
            when(listMyCaregiversUseCase.execute(patientId)).thenReturn(List.of(
                    new ListMyCaregiversUseCase.CaregiverView(
                            UUID.randomUUID(), UUID.randomUUID(), "Carlos Pérez", "carlos@example.com", linkedAt)));

            mockMvc.perform(get("/api/v1/caregivers/{patientId}/links", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].caregiverName").value("Carlos Pérez"))
                    .andExpect(jsonPath("$[0].caregiverEmail").value("carlos@example.com"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/caregivers/{patientId}/links/{linkId}")
    class RevokeLink {

        @Test
        @DisplayName("retorna 204 al revocar correctamente")
        void returns204OnSuccessfulRevoke() throws Exception {
            UUID linkId = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/caregivers/{patientId}/links/{linkId}", patientId, linkId))
                    .andExpect(status().isNoContent());

            verify(revokeCaregiverAccessUseCase).execute(
                    new RevokeCaregiverAccessUseCase.Command(patientId, linkId));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/caregivers/redeem")
    class Redeem {

        @Test
        @DisplayName("retorna 200 con el paciente al que se ganó acceso")
        void returns200WithPatientGrantedAccessTo() throws Exception {
            UUID caregiverUserId = UUID.randomUUID();
            when(currentUserResolver.resolveUserId(any())).thenReturn(caregiverUserId);
            when(redeemCaregiverInviteUseCase.execute(any()))
                    .thenReturn(new RedeemCaregiverInviteUseCase.Result(patientId, "Ana García"));

            String body = """
                    {"code":"ABCD-1234"}
                    """;

            mockMvc.perform(post("/api/v1/caregivers/redeem")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patientFullName").value("Ana García"));

            verify(redeemCaregiverInviteUseCase).execute(
                    new RedeemCaregiverInviteUseCase.Command("ABCD-1234", caregiverUserId));
        }

        @Test
        @DisplayName("retorna 400 cuando el código está en blanco")
        void returns400WhenCodeIsBlank() throws Exception {
            String body = """
                    {"code":""}
                    """;

            mockMvc.perform(post("/api/v1/caregivers/redeem")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(redeemCaregiverInviteUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/caregivers/my-patients")
    class GetMyPatients {

        @Test
        @DisplayName("retorna 200 con los pacientes que cuida, mapeados correctamente")
        void returns200WithPatientsMappedCorrectly() throws Exception {
            UUID caregiverUserId = UUID.randomUUID();
            when(currentUserResolver.resolveUserId(any())).thenReturn(caregiverUserId);
            when(listPatientsICareForUseCase.execute(caregiverUserId)).thenReturn(List.of(
                    new ListPatientsICareForUseCase.PatientAccessView(
                            patientId, "Ana García", LocalDateTime.now())));

            mockMvc.perform(get("/api/v1/caregivers/my-patients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].patientFullName").value("Ana García"));
        }
    }
}
