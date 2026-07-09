package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReminderPort;
import com.diabecare.application.port.out.SaveGlucoseReminderPort;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.domain.model.GlucoseReminder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToggleGlucoseReminderUseCaseImpl")
class ToggleGlucoseReminderUseCaseTest {

    @Mock
    private LoadGlucoseReminderPort loadGlucoseReminderPort;
    @Mock
    private SaveGlucoseReminderPort saveGlucoseReminderPort;

    @InjectMocks
    private ToggleGlucoseReminderUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();
    private final UUID reminderId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("deshabilita un recordatorio habilitado y conserva el resto de sus datos")
        void disablesEnabledReminderAndKeepsRestOfItsData() {
            GlucoseReminder existing = GlucoseReminder.builder()
                    .id(reminderId).patientId(patientId)
                    .reminderTime(LocalTime.of(7, 0)).label("Ayunas").enabled(true).build();
            when(loadGlucoseReminderPort.findById(reminderId)).thenReturn(Optional.of(existing));
            when(saveGlucoseReminderPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

            GlucoseReminder result = useCase.execute(patientId, reminderId, false);

            assertThat(result.isEnabled()).isFalse();
            assertThat(result.getLabel()).isEqualTo("Ayunas");
            assertThat(result.getReminderTime()).isEqualTo(LocalTime.of(7, 0));

            ArgumentCaptor<GlucoseReminder> captor = ArgumentCaptor.forClass(GlucoseReminder.class);
            verify(saveGlucoseReminderPort).save(captor.capture());
            assertThat(captor.getValue().isEnabled()).isFalse();
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando el recordatorio no existe")
        void throwsUnauthorizedWhenReminderDoesNotExist() {
            when(loadGlucoseReminderPort.findById(reminderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(patientId, reminderId, false))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);

            verifyNoInteractions(saveGlucoseReminderPort);
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando el recordatorio pertenece a otro paciente")
        void throwsUnauthorizedWhenReminderBelongsToAnotherPatient() {
            GlucoseReminder othersReminder = GlucoseReminder.builder()
                    .id(reminderId).patientId(UUID.randomUUID())
                    .reminderTime(LocalTime.of(7, 0)).enabled(true).build();
            when(loadGlucoseReminderPort.findById(reminderId)).thenReturn(Optional.of(othersReminder));

            assertThatThrownBy(() -> useCase.execute(patientId, reminderId, false))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);

            verifyNoInteractions(saveGlucoseReminderPort);
        }
    }
}
