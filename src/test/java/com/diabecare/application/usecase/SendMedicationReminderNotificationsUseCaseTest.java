package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadMedicationPort;
import com.diabecare.application.port.out.NotifyPatientPort;
import com.diabecare.domain.model.DoseUnit;
import com.diabecare.domain.model.Medication;
import com.diabecare.domain.model.MedicationFrequency;
import com.diabecare.domain.model.MedicationType;
import com.diabecare.domain.service.MedicationReminderTimeResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendMedicationReminderNotificationsUseCaseImpl")
class SendMedicationReminderNotificationsUseCaseTest {

    @Mock
    private LoadMedicationPort loadMedicationPort;
    @Mock
    private NotifyPatientPort notifyPatientPort;

    private final UUID patientId = UUID.randomUUID();

    @Test
    @DisplayName("no notifica cuando no hay medicamentos activos")
    void doesNotNotifyWhenNoActiveMedications() {
        when(loadMedicationPort.findAllActive()).thenReturn(List.of());

        useCaseAt(8, 0).execute();

        verifyNoInteractions(notifyPatientPort);
    }

    @Test
    @DisplayName("nunca notifica un medicamento AS_NEEDED, sin importar la hora")
    void neverNotifiesAsNeededMedication() {
        when(loadMedicationPort.findAllActive()).thenReturn(List.of(medication(MedicationFrequency.AS_NEEDED)));

        useCaseAt(8, 0).execute();

        verifyNoInteractions(notifyPatientPort);
    }

    @Test
    @DisplayName("notifica cuando la hora actual coincide con el horario de ONCE_DAILY (08:00)")
    void notifiesWhenCurrentTimeMatchesOnceDailySchedule() {
        when(loadMedicationPort.findAllActive()).thenReturn(List.of(medication(MedicationFrequency.ONCE_DAILY)));

        useCaseAt(8, 0).execute();

        verify(notifyPatientPort).notify(eq(patientId), any(), any());
    }

    @Test
    @DisplayName("no notifica cuando la hora actual NO coincide con ningún horario de la frecuencia")
    void doesNotNotifyWhenCurrentTimeDoesNotMatchSchedule() {
        when(loadMedicationPort.findAllActive()).thenReturn(List.of(medication(MedicationFrequency.ONCE_DAILY)));

        useCaseAt(9, 30).execute();

        verifyNoInteractions(notifyPatientPort);
    }

    @Test
    @DisplayName("notifica dos veces para TWICE_DAILY en su segundo horario (20:00)")
    void notifiesForTwiceDailyAtItsSecondSchedule() {
        when(loadMedicationPort.findAllActive()).thenReturn(List.of(medication(MedicationFrequency.TWICE_DAILY)));

        useCaseAt(20, 0).execute();

        verify(notifyPatientPort).notify(eq(patientId), any(), any());
    }

    private SendMedicationReminderNotificationsUseCaseImpl useCaseAt(int hour, int minute) {
        Clock fixedClock = Clock.fixed(
                LocalDate.now().atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault());
        return new SendMedicationReminderNotificationsUseCaseImpl(
                loadMedicationPort, new MedicationReminderTimeResolver(), notifyPatientPort, fixedClock);
    }

    private Medication medication(MedicationFrequency frequency) {
        return Medication.builder()
                .medicationId(UUID.randomUUID())
                .patientId(patientId)
                .name("Metformina")
                .type(MedicationType.ORAL)
                .dose(BigDecimal.valueOf(500))
                .doseUnit(DoseUnit.MG)
                .frequency(frequency)
                .startDate(LocalDate.now().minusDays(30))
                .active(true)
                .build();
    }
}
