package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.CreateGlucoseReminderUseCase;
import com.diabecare.application.port.in.DeleteGlucoseReminderUseCase;
import com.diabecare.application.port.in.GetGlucoseRemindersUseCase;
import com.diabecare.application.port.in.ToggleGlucoseReminderUseCase;
import com.diabecare.domain.model.GlucoseReminder;
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

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlucoseReminderController")
class GlucoseReminderControllerTest {

    @Mock private CreateGlucoseReminderUseCase createGlucoseReminderUseCase;
    @Mock private GetGlucoseRemindersUseCase getGlucoseRemindersUseCase;
    @Mock private ToggleGlucoseReminderUseCase toggleGlucoseReminderUseCase;
    @Mock private DeleteGlucoseReminderUseCase deleteGlucoseReminderUseCase;
    @Mock private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID patientId = UUID.randomUUID();
    private final UUID reminderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        GlucoseReminderController controller = new GlucoseReminderController(
                createGlucoseReminderUseCase, getGlucoseRemindersUseCase,
                toggleGlucoseReminderUseCase, deleteGlucoseReminderUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Nested @DisplayName("GET /api/v1/glucose-reminders/{patientId}")
    class GetAll {

        @Test @DisplayName("retorna 200 con los recordatorios mapeados correctamente")
        void returns200WithRemindersMappedCorrectly() throws Exception {
            GlucoseReminder reminder = validReminder();
            when(getGlucoseRemindersUseCase.execute(patientId)).thenReturn(List.of(reminder));

            mockMvc.perform(get("/api/v1/glucose-reminders/{patientId}", patientId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].label").value("Ayunas"))
                    .andExpect(jsonPath("$[0].enabled").value(true));
        }
    }

    @Nested @DisplayName("POST /api/v1/glucose-reminders/{patientId}")
    class Create {

        @Test @DisplayName("retorna 201 con el recordatorio creado")
        void returns201WithCreatedReminder() throws Exception {
            when(createGlucoseReminderUseCase.execute(any())).thenReturn(validReminder());

            mockMvc.perform(post("/api/v1/glucose-reminders/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"reminderTime\":\"07:00\",\"label\":\"Ayunas\"}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.label").value("Ayunas"));
        }

        @Test @DisplayName("retorna 400 cuando no se envía la hora del recordatorio")
        void returns400WhenReminderTimeIsMissing() throws Exception {
            mockMvc.perform(post("/api/v1/glucose-reminders/{patientId}", patientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"label\":\"Ayunas\"}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(createGlucoseReminderUseCase);
        }
    }

    @Nested @DisplayName("PATCH /api/v1/glucose-reminders/{patientId}/{reminderId}")
    class Toggle {

        @Test @DisplayName("retorna 200 y ejecuta el cambio de estado")
        void returns200AndExecutesToggle() throws Exception {
            when(toggleGlucoseReminderUseCase.execute(patientId, reminderId, false))
                    .thenReturn(validReminder());

            mockMvc.perform(patch("/api/v1/glucose-reminders/{patientId}/{reminderId}", patientId, reminderId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"enabled\":false}"))
                    .andExpect(status().isOk());

            verify(toggleGlucoseReminderUseCase).execute(patientId, reminderId, false);
        }
    }

    @Nested @DisplayName("DELETE /api/v1/glucose-reminders/{patientId}/{reminderId}")
    class Delete {

        @Test @DisplayName("retorna 204 y ejecuta el borrado")
        void returns204AndExecutesDeletion() throws Exception {
            mockMvc.perform(delete("/api/v1/glucose-reminders/{patientId}/{reminderId}", patientId, reminderId))
                    .andExpect(status().isNoContent());

            verify(deleteGlucoseReminderUseCase).execute(patientId, reminderId);
        }
    }

    private GlucoseReminder validReminder() {
        return GlucoseReminder.builder()
                .id(reminderId).patientId(patientId)
                .reminderTime(LocalTime.of(7, 0)).label("Ayunas").enabled(true).build();
    }
}
