package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetHba1cTrendUseCase;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetHba1cTrendUseCaseImpl")
class GetHba1cTrendUseCaseTest {

    @Mock
    private LoadGlucoseReadingPort loadGlucoseReadingPort;

    private GetHba1cTrendUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new GetHba1cTrendUseCaseImpl(loadGlucoseReadingPort, new MedicalCalculatorService());
    }

    @Nested
    @DisplayName("getTrend")
    class GetTrend {

        @Test
        @DisplayName("retorna exactamente la cantidad de meses solicitada")
        void returnsExactlyTheRequestedNumberOfMonths() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                    .thenReturn(List.of());

            List<GetHba1cTrendUseCase.MonthlyHba1c> trend = useCase.getTrend(patientId, 3);

            assertThat(trend).hasSize(3);
        }

        @Test
        @DisplayName("marca un mes sin lecturas con hba1c y promedio nulos, y cero lecturas")
        void marksMonthWithoutReadingsAsNull() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                    .thenReturn(List.of());

            List<GetHba1cTrendUseCase.MonthlyHba1c> trend = useCase.getTrend(patientId, 1);

            assertThat(trend.get(0).estimatedHba1c()).isNull();
            assertThat(trend.get(0).averageGlucose()).isNull();
            assertThat(trend.get(0).totalReadings()).isZero();
        }

        @Test
        @DisplayName("calcula hba1c y promedio correctos para un mes con lecturas")
        void calculatesHba1cAndAverageForMonthWithReadings() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(100));
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                    .thenReturn(List.of(reading));

            List<GetHba1cTrendUseCase.MonthlyHba1c> trend = useCase.getTrend(patientId, 1);

            assertThat(trend.get(0).averageGlucose()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(trend.get(0).estimatedHba1c()).isNotNull();
            assertThat(trend.get(0).totalReadings()).isEqualTo(1);
        }

        @Test
        @DisplayName("consulta el puerto de lecturas una vez por cada mes solicitado")
        void queriesReadingsPortOncePerRequestedMonth() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                    .thenReturn(List.of());

            useCase.getTrend(patientId, 3);

            verify(loadGlucoseReadingPort, times(3))
                    .findByPatientIdAndDateRange(eq(patientId), any(), any());
        }

        @Test
        @DisplayName("consulta cada mes con el rango horario completo (00:00 a 23:59)")
        void queriesEachMonthWithFullDayRange() {
            when(loadGlucoseReadingPort.findByPatientIdAndDateRange(any(), any(), any()))
                    .thenReturn(List.of());

            useCase.getTrend(patientId, 1);

            verify(loadGlucoseReadingPort).findByPatientIdAndDateRange(
                    eq(patientId),
                    argThat(from -> from.getHour() == 0 && from.getMinute() == 0),
                    argThat(to -> to.getHour() == 23 && to.getMinute() == 59));
        }
    }


    private GlucoseReading readingWith(BigDecimal value) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(patientId)
                .value(value)
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}
