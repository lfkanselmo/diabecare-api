package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.NotifyPatientPort;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.domain.service.MedicalCalculatorService;
import com.diabecare.domain.service.WeeklySummaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SendWeeklySummaryUseCaseImpl")
class SendWeeklySummaryUseCaseTest {

    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock
    private NotifyPatientPort notifyPatientPort;

    private SendWeeklySummaryUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        var weeklySummaryService = new WeeklySummaryService(
                new MedicalCalculatorService(), (key, args) -> "texto de prueba");
        useCase = new SendWeeklySummaryUseCaseImpl(
                loadPatientPort, loadGlucoseReadingPort, weeklySummaryService, notifyPatientPort);
    }

    @Nested
    @DisplayName("sendToAllPatients")
    class SendToAllPatients {

        @Test
        @DisplayName("envía la notificación a un paciente que tiene lecturas en la última semana")
        void sendsNotificationToPatientWithReadings() {
            Patient patient = validPatient();
            when(loadPatientPort.findAll()).thenReturn(List.of(patient));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(patient.getPatientId()), any(), any()))
                    .thenReturn(List.of(readingWith(BigDecimal.valueOf(100))));

            useCase.sendToAllPatients();

            verify(notifyPatientPort).notify(eq(patient.getPatientId()), any(), any());
        }

        @Test
        @DisplayName("no envía notificación a un paciente sin lecturas en la última semana")
        void doesNotSendNotificationToPatientWithoutReadings() {
            Patient patient = validPatient();
            when(loadPatientPort.findAll()).thenReturn(List.of(patient));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(patient.getPatientId()), any(), any()))
                    .thenReturn(List.of());

            useCase.sendToAllPatients();

            verifyNoInteractions(notifyPatientPort);
        }

        @Test
        @DisplayName("procesa cada paciente de forma independiente, enviando solo a quienes corresponde")
        void processesEachPatientIndependently() {
            Patient withReadings = validPatient();
            Patient withoutReadings = validPatient();

            when(loadPatientPort.findAll()).thenReturn(List.of(withReadings, withoutReadings));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(withReadings.getPatientId()), any(), any()))
                    .thenReturn(List.of(readingWith(BigDecimal.valueOf(100))));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(eq(withoutReadings.getPatientId()), any(), any()))
                    .thenReturn(List.of());

            useCase.sendToAllPatients();

            verify(notifyPatientPort, times(1)).notify(eq(withReadings.getPatientId()), any(), any());
            verify(notifyPatientPort, never()).notify(eq(withoutReadings.getPatientId()), any(), any());
        }

        @Test
        @DisplayName("no falla y no envía nada cuando no hay pacientes registrados")
        void doesNothingWhenNoPatientsExist() {
            when(loadPatientPort.findAll()).thenReturn(List.of());

            useCase.sendToAllPatients();

            verifyNoInteractions(notifyPatientPort, loadGlucoseReadingPort);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Patient validPatient() {
        return Patient.create(
                UUID.randomUUID(), "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }

    private GlucoseReading readingWith(BigDecimal value) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(UUID.randomUUID())
                .value(value)
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}