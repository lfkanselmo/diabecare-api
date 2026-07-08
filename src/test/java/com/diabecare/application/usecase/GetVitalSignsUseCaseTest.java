package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadVitalSignPort;
import com.diabecare.domain.model.VitalSign;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetVitalSignsUseCaseImpl")
class GetVitalSignsUseCaseTest {

    @Mock
    private LoadVitalSignPort loadVitalSignPort;

    @InjectMocks
    private GetVitalSignsUseCaseImpl useCase;

    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("getByPatientId")
    class GetByPatientId {

        @Test
        @DisplayName("retorna la página de signos vitales del paciente")
        void returnsAllVitalSignsForPatient() {
            VitalSign vital = vitalWith(BigDecimal.valueOf(70));
            Pageable pageable = PageRequest.of(0, 20);
            when(loadVitalSignPort.findByPatientId(patientId, pageable))
                    .thenReturn(new PageImpl<>(List.of(vital), pageable, 1));

            Page<VitalSign> result = useCase.getByPatientId(patientId, pageable);

            assertThat(result.getContent()).containsExactly(vital);
        }

        @Test
        @DisplayName("retorna página vacía cuando el paciente no tiene signos vitales registrados")
        void returnsEmptyListWhenNoVitalSigns() {
            Pageable pageable = PageRequest.of(0, 20);
            when(loadVitalSignPort.findByPatientId(patientId, pageable))
                    .thenReturn(Page.empty(pageable));

            Page<VitalSign> result = useCase.getByPatientId(patientId, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getLatest")
    class GetLatest {

        @Test
        @DisplayName("retorna el signo vital más reciente cuando existe")
        void returnsLatestVitalSignWhenExists() {
            VitalSign vital = vitalWith(BigDecimal.valueOf(70));
            when(loadVitalSignPort.findLatestByPatientId(patientId)).thenReturn(Optional.of(vital));

            Optional<VitalSign> result = useCase.getLatest(patientId);

            assertThat(result).contains(vital);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando no hay signos vitales registrados")
        void returnsEmptyWhenNoVitalSignsExist() {
            when(loadVitalSignPort.findLatestByPatientId(patientId)).thenReturn(Optional.empty());

            Optional<VitalSign> result = useCase.getLatest(patientId);

            assertThat(result).isEmpty();
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private VitalSign vitalWith(BigDecimal weightKg) {
        return VitalSign.builder()
                .vitalId(UUID.randomUUID())
                .patientId(patientId)
                .weightKg(weightKg)
                .measuredAt(LocalDateTime.now())
                .build();
    }
}