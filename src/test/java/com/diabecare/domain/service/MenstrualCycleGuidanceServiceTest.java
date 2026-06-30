package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.CyclePhase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenstrualCycleGuidanceService")
class MenstrualCycleGuidanceServiceTest {

    @Mock
    private MessageResolverPort messages;

    private MenstrualCycleGuidanceService service;

    @BeforeEach
    void setUp() {
        service = new MenstrualCycleGuidanceService(messages);
    }

    @Nested
    @DisplayName("resolveGuidance")
    class ResolveGuidance {

        @Test
        @DisplayName("resuelve la clave de guía correcta para cada fase")
        void resolvesCorrectGuidanceKeyForEachPhase() {
            when(messages.resolve(any())).thenReturn("guía");

            service.resolveGuidance(CyclePhase.MENSTRUATION);
            verify(messages).resolve("cycle.guidance.menstruation");

            service.resolveGuidance(CyclePhase.FOLLICULAR);
            verify(messages).resolve("cycle.guidance.follicular");

            service.resolveGuidance(CyclePhase.OVULATION);
            verify(messages).resolve("cycle.guidance.ovulation");

            service.resolveGuidance(CyclePhase.LUTEAL_EARLY);
            verify(messages).resolve("cycle.guidance.luteal-early");

            service.resolveGuidance(CyclePhase.LUTEAL_LATE);
            verify(messages).resolve("cycle.guidance.luteal-late");
        }

        @Test
        @DisplayName("retorna el valor resuelto por el puerto de mensajes")
        void returnsValueFromMessagePort() {
            when(messages.resolve("cycle.guidance.ovulation")).thenReturn("Cuidado especial");

            String guidance = service.resolveGuidance(CyclePhase.OVULATION);

            assertThat(guidance).isEqualTo("Cuidado especial");
        }
    }

    @Nested
    @DisplayName("resolveLabel")
    class ResolveLabel {

        @Test
        @DisplayName("resuelve la clave de etiqueta correcta para cada fase")
        void resolvesCorrectLabelKeyForEachPhase() {
            when(messages.resolve(any())).thenReturn("etiqueta");

            service.resolveLabel(CyclePhase.MENSTRUATION);
            verify(messages).resolve("cycle.label.menstruation");

            service.resolveLabel(CyclePhase.FOLLICULAR);
            verify(messages).resolve("cycle.label.follicular");

            service.resolveLabel(CyclePhase.OVULATION);
            verify(messages).resolve("cycle.label.ovulation");

            service.resolveLabel(CyclePhase.LUTEAL_EARLY);
            verify(messages).resolve("cycle.label.luteal-early");

            service.resolveLabel(CyclePhase.LUTEAL_LATE);
            verify(messages).resolve("cycle.label.luteal-late");
        }

        @Test
        @DisplayName("retorna el valor resuelto por el puerto de mensajes")
        void returnsValueFromMessagePort() {
            when(messages.resolve("cycle.label.follicular")).thenReturn("Fase folicular");

            String label = service.resolveLabel(CyclePhase.FOLLICULAR);

            assertThat(label).isEqualTo("Fase folicular");
        }
    }
}