package com.diabecare.application.usecase;

import com.diabecare.application.port.in.CreateGlucoseReminderUseCase;
import com.diabecare.application.port.out.SaveGlucoseReminderPort;
import com.diabecare.domain.model.GlucoseReminder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateGlucoseReminderUseCaseImpl")
class CreateGlucoseReminderUseCaseTest {

    @Mock
    private SaveGlucoseReminderPort saveGlucoseReminderPort;

    @InjectMocks
    private CreateGlucoseReminderUseCaseImpl useCase;

    @Test
    @DisplayName("crea el recordatorio habilitado por defecto con los datos del comando")
    void createsReminderEnabledByDefaultWithCommandData() {
        UUID patientId = UUID.randomUUID();
        when(saveGlucoseReminderPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        GlucoseReminder result = useCase.execute(
                new CreateGlucoseReminderUseCase.Command(patientId, LocalTime.of(7, 0), "Ayunas"));

        assertThat(result.getPatientId()).isEqualTo(patientId);
        assertThat(result.getReminderTime()).isEqualTo(LocalTime.of(7, 0));
        assertThat(result.getLabel()).isEqualTo("Ayunas");
        assertThat(result.isEnabled()).isTrue();

        ArgumentCaptor<GlucoseReminder> captor = ArgumentCaptor.forClass(GlucoseReminder.class);
        verify(saveGlucoseReminderPort).save(captor.capture());
        assertThat(captor.getValue().isEnabled()).isTrue();
    }
}
