package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadGlucoseReminderPort;
import com.diabecare.application.port.out.NotifyPatientPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseReminder;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendGlucoseReminderNotificationsUseCaseImpl")
class SendGlucoseReminderNotificationsUseCaseTest {

    @Mock
    private LoadGlucoseReminderPort loadGlucoseReminderPort;
    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock
    private NotifyPatientPort notifyPatientPort;

    private final UUID patientId = UUID.randomUUID();
    private final LocalDateTime fixedNow = LocalDate.now().atTime(7, 0);

    @Test
    @DisplayName("no notifica cuando no hay recordatorios habilitados a esta hora")
    void doesNotNotifyWhenNoRemindersEnabledAtThisTime() {
        when(loadGlucoseReminderPort.findAllEnabledAtTime(LocalTime.of(7, 0))).thenReturn(List.of());

        useCase().execute();

        verifyNoInteractions(notifyPatientPort);
    }

    @Test
    @DisplayName("notifica cuando hay un recordatorio habilitado y el paciente no midió recientemente")
    void notifiesWhenReminderEnabledAndNoRecentReading() {
        when(loadGlucoseReminderPort.findAllEnabledAtTime(LocalTime.of(7, 0)))
                .thenReturn(List.of(reminder("Ayunas")));
        when(loadGlucoseReadingPort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());

        useCase().execute();

        verify(notifyPatientPort).notify(eq(patientId), any(), eq("Recordatorio: Ayunas"));
    }

    @Test
    @DisplayName("usa un mensaje genérico cuando el recordatorio no tiene etiqueta")
    void usesGenericMessageWhenReminderHasNoLabel() {
        when(loadGlucoseReminderPort.findAllEnabledAtTime(LocalTime.of(7, 0)))
                .thenReturn(List.of(reminder(null)));
        when(loadGlucoseReadingPort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());

        useCase().execute();

        verify(notifyPatientPort).notify(eq(patientId), any(), eq("Es hora de registrar tu glucosa"));
    }

    @Test
    @DisplayName("no notifica cuando el paciente ya midió su glucosa hace menos de 30 minutos")
    void doesNotNotifyWhenPatientAlreadyMeasuredRecently() {
        when(loadGlucoseReminderPort.findAllEnabledAtTime(LocalTime.of(7, 0)))
                .thenReturn(List.of(reminder("Ayunas")));
        when(loadGlucoseReadingPort.findLatestByPatientId(patientId))
                .thenReturn(Optional.of(readingAt(fixedNow.minusMinutes(10))));

        useCase().execute();

        verifyNoInteractions(notifyPatientPort);
    }

    @Test
    @DisplayName("sí notifica cuando la última medición fue hace más de 30 minutos")
    void notifiesWhenLastReadingWasMoreThan30MinutesAgo() {
        when(loadGlucoseReminderPort.findAllEnabledAtTime(LocalTime.of(7, 0)))
                .thenReturn(List.of(reminder("Ayunas")));
        when(loadGlucoseReadingPort.findLatestByPatientId(patientId))
                .thenReturn(Optional.of(readingAt(fixedNow.minusMinutes(45))));

        useCase().execute();

        verify(notifyPatientPort).notify(eq(patientId), any(), any());
    }

    private SendGlucoseReminderNotificationsUseCaseImpl useCase() {
        Clock fixedClock = Clock.fixed(
                fixedNow.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        return new SendGlucoseReminderNotificationsUseCaseImpl(
                loadGlucoseReminderPort, loadGlucoseReadingPort, notifyPatientPort, fixedClock);
    }

    private GlucoseReminder reminder(String label) {
        return GlucoseReminder.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .reminderTime(LocalTime.of(7, 0))
                .label(label)
                .enabled(true)
                .build();
    }

    private GlucoseReading readingAt(LocalDateTime measuredAt) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(patientId)
                .value(BigDecimal.valueOf(110))
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.FASTING)
                .measuredAt(measuredAt)
                .build();
    }
}
