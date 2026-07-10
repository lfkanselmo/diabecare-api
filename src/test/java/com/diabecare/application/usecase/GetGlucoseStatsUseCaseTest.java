package com.diabecare.application.usecase;

import com.diabecare.application.dto.GlucoseStatsRecord;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.domain.service.MedicalCalculatorService;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetGlucoseStatsUseCaseImpl")
class GetGlucoseStatsUseCaseTest {

    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;
    @Mock
    private LoadPatientPort loadPatientPort;

    private GetGlucoseStatsUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();
    private final LocalDateTime from = LocalDateTime.now().minusDays(7);
    private final LocalDateTime to = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        useCase = new GetGlucoseStatsUseCaseImpl(
                loadGlucoseReadingPort, loadPatientPort, new MedicalCalculatorService());
    }

    @Nested
    @DisplayName("getStats")
    class GetStats {

        @Test
        @DisplayName("lanza PatientNotFoundException cuando el paciente no existe")
        void throwsWhenPatientNotFound() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.getStats(patientId, from, to))
                    .isInstanceOf(PatientNotFoundException.class);
        }

        @Test
        @DisplayName("retorna estadísticas vacías (todo en cero) cuando no hay lecturas")
        void returnsEmptyStatsWhenNoReadings() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(List.of());

            GlucoseStatsRecord stats = useCase.getStats(patientId, from, to);

            assertThat(stats.average()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(stats.standardDeviation()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(stats.totalReadings()).isZero();
        }

        @Test
        @DisplayName("calcula las estadísticas completas cuando hay lecturas")
        void calculatesCompleteStatsWhenReadingsExist() {
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(validPatient()));
            List<GlucoseReading> readings = readingsWith(100, 120, 80, 110, 90);
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(readings);

            GlucoseStatsRecord stats = useCase.getStats(patientId, from, to);

            assertThat(stats.totalReadings()).isEqualTo(5);
            assertThat(stats.average()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2));
        }

        @Test
        @DisplayName("usa el rango objetivo del paciente para calcular tiempo en rango")
        void usesPatientTargetRangeForTir() {
            Patient patient = validPatient();
            patient.updateGlucoseTarget(BigDecimal.valueOf(80), BigDecimal.valueOf(160));
            when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

            // todas dentro del rango personalizado 80-160
            List<GlucoseReading> readings = readingsWith(90, 100, 110, 120, 150);
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(patientId, from, to))
                    .thenReturn(readings);

            GlucoseStatsRecord stats = useCase.getStats(patientId, from, to);

            assertThat(stats.timeInRangePercent()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2));
        }
    }


    private Patient validPatient() {
        return Patient.create(
                patientId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }

    private List<GlucoseReading> readingsWith(int... values) {
        return java.util.Arrays.stream(values)
                .mapToObj(v -> GlucoseReading.builder()
                        .readingId(UUID.randomUUID())
                        .patientId(patientId)
                        .value(BigDecimal.valueOf(v))
                        .unit(GlucoseUnit.MG_DL)
                        .readingType(ReadingType.RANDOM)
                        .measuredAt(LocalDateTime.now())
                        .build())
                .toList();
    }
}
