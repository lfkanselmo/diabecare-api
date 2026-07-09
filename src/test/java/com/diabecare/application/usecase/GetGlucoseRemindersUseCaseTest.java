package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReminderPort;
import com.diabecare.domain.model.GlucoseReminder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetGlucoseRemindersUseCaseImpl")
class GetGlucoseRemindersUseCaseTest {

    @Mock
    private LoadGlucoseReminderPort loadGlucoseReminderPort;

    @InjectMocks
    private GetGlucoseRemindersUseCaseImpl useCase;

    @Test
    @DisplayName("retorna los recordatorios del paciente provistos por el puerto")
    void returnsPatientRemindersFromPort() {
        UUID patientId = UUID.randomUUID();
        GlucoseReminder reminder = GlucoseReminder.builder()
                .id(UUID.randomUUID()).patientId(patientId)
                .reminderTime(LocalTime.of(7, 0)).enabled(true).build();
        when(loadGlucoseReminderPort.findByPatientId(patientId)).thenReturn(List.of(reminder));

        List<GlucoseReminder> result = useCase.execute(patientId);

        assertThat(result).containsExactly(reminder);
    }
}
