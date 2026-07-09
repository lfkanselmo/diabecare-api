package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ImportGlucoseReadingsUseCase;
import com.diabecare.application.port.out.DeviceApiKeyPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveGlucoseReadingPort;
import com.diabecare.domain.exception.InvalidDeviceApiKeyException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.*;
import com.diabecare.domain.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ImportGlucoseReadingsUseCaseImpl")
class ImportGlucoseReadingsUseCaseTest {

    @Mock
    private DeviceApiKeyPort deviceApiKeyPort;
    @Mock
    private SaveGlucoseReadingPort saveGlucoseReadingPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private RateLimitService rateLimitService;

    @InjectMocks
    private ImportGlucoseReadingsUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();
    private final String rawKey = "dbc_rawkey123";

    @BeforeEach
    void setUp() {
        doNothing().when(rateLimitService).checkDeviceImportLimit(any());
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(buildPatient(patientId)));
        when(saveGlucoseReadingPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("importa cada lectura del batch etiquetada con el label de la key")
    void importsEachReadingInBatchTaggedWithKeyLabel() {
        DeviceApiKey key = validKey();
        when(deviceApiKeyPort.findByRawKey(rawKey)).thenReturn(Optional.of(key));

        var command = new ImportGlucoseReadingsUseCase.Command(rawKey, List.of(
                new ImportGlucoseReadingsUseCase.ReadingInput(
                        new BigDecimal("120"), GlucoseUnit.MG_DL, ReadingType.RANDOM,
                        LocalDateTime.now().minusMinutes(10)),
                new ImportGlucoseReadingsUseCase.ReadingInput(
                        new BigDecimal("130"), GlucoseUnit.MG_DL, ReadingType.RANDOM,
                        LocalDateTime.now().minusMinutes(5))
        ));

        List<GlucoseReading> result = useCase.execute(command);

        assertThat(result).hasSize(2);
        assertThat(result).allSatisfy(r -> assertThat(r.getDeviceSource()).isEqualTo("Dexcom G6"));
        verify(saveGlucoseReadingPort, times(2)).save(any());
        verify(rateLimitService, times(2)).checkDeviceImportLimit(key.getId());
        verify(deviceApiKeyPort).touchLastUsed(key.getId());
    }

    @Test
    @DisplayName("lanza excepción cuando la key no existe")
    void throwsExceptionWhenKeyDoesNotExist() {
        when(deviceApiKeyPort.findByRawKey(rawKey)).thenReturn(Optional.empty());

        var command = new ImportGlucoseReadingsUseCase.Command(rawKey, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidDeviceApiKeyException.class);

        verify(saveGlucoseReadingPort, never()).save(any());
    }

    @Test
    @DisplayName("lanza excepción cuando la key fue revocada")
    void throwsExceptionWhenKeyIsRevoked() {
        DeviceApiKey revokedKey = DeviceApiKey.builder()
                .id(UUID.randomUUID()).patientId(patientId).label("Dexcom G6")
                .revokedAt(LocalDateTime.now().minusDays(1)).build();
        when(deviceApiKeyPort.findByRawKey(rawKey)).thenReturn(Optional.of(revokedKey));

        var command = new ImportGlucoseReadingsUseCase.Command(rawKey, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(InvalidDeviceApiKeyException.class);

        verify(saveGlucoseReadingPort, never()).save(any());
    }

    @Test
    @DisplayName("lanza excepción cuando el paciente de la key ya no existe")
    void throwsExceptionWhenKeyPatientNoLongerExists() {
        DeviceApiKey key = validKey();
        when(deviceApiKeyPort.findByRawKey(rawKey)).thenReturn(Optional.of(key));
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.empty());

        var command = new ImportGlucoseReadingsUseCase.Command(rawKey, List.of());

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(PatientNotFoundException.class);

        verify(saveGlucoseReadingPort, never()).save(any());
    }

    private DeviceApiKey validKey() {
        return DeviceApiKey.builder()
                .id(UUID.randomUUID()).patientId(patientId).label("Dexcom G6")
                .createdAt(LocalDateTime.now()).build();
    }

    private Patient buildPatient(UUID id) {
        return Patient.builder()
                .patientId(id)
                .userId(UUID.randomUUID())
                .fullName("Test Patient")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .diabetesType(DiabetesType.TYPE_2)
                .diagnosisDate(LocalDate.of(2020, 1, 1))
                .heightCm(new BigDecimal("170"))
                .targetGlucoseMin(new BigDecimal("70"))
                .targetGlucoseMax(new BigDecimal("180"))
                .activityLevel(ActivityLevel.MODERATELY_ACTIVE)
                .preferredGlucoseUnit(GlucoseUnit.MG_DL)
                .biologicalSex(BiologicalSex.NOT_SPECIFIED)
                .build();
    }
}
