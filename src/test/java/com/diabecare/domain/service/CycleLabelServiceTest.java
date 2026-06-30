package com.diabecare.domain.service;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.domain.model.CycleSymptom;
import com.diabecare.domain.model.FlowIntensity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CycleLabelService")
class CycleLabelServiceTest {

    @Mock
    private MessageResolverPort messages;

    private CycleLabelService service;

    @BeforeEach
    void setUp() {
        service = new CycleLabelService(messages);
    }

    @Nested
    @DisplayName("resolveFlowLabel")
    class ResolveFlowLabel {

        @Test
        @DisplayName("resuelve la clave correcta para cada intensidad de flujo")
        void resolvesCorrectKeyForEachIntensity() {
            when(messages.resolve(any())).thenReturn("etiqueta");

            service.resolveFlowLabel(FlowIntensity.NONE);
            verify(messages).resolve("cycle.flow.none");

            service.resolveFlowLabel(FlowIntensity.SPOTTING);
            verify(messages).resolve("cycle.flow.spotting");

            service.resolveFlowLabel(FlowIntensity.LIGHT);
            verify(messages).resolve("cycle.flow.light");

            service.resolveFlowLabel(FlowIntensity.MODERATE);
            verify(messages).resolve("cycle.flow.moderate");

            service.resolveFlowLabel(FlowIntensity.HEAVY);
            verify(messages).resolve("cycle.flow.heavy");

            service.resolveFlowLabel(FlowIntensity.VERY_HEAVY);
            verify(messages).resolve("cycle.flow.very-heavy");
        }

        @Test
        @DisplayName("retorna el valor resuelto por el puerto de mensajes")
        void returnsValueFromMessagePort() {
            when(messages.resolve("cycle.flow.light")).thenReturn("Ligero");

            String label = service.resolveFlowLabel(FlowIntensity.LIGHT);

            assertThat(label).isEqualTo("Ligero");
        }
    }

    @Nested
    @DisplayName("resolveSymptomLabel")
    class ResolveSymptomLabel {

        @Test
        @DisplayName("construye la clave a partir del nombre del síntoma en minúsculas con guiones")
        void buildsKeyFromSymptomName() {
            when(messages.resolve(any())).thenReturn("etiqueta");

            service.resolveSymptomLabel(CycleSymptom.MOOD_CHANGES);

            verify(messages).resolve("cycle.symptom.mood-changes");
        }

        @ParameterizedTest(name = "{0}")
        @EnumSource(CycleSymptom.class)
        @DisplayName("construye una clave válida (sin guiones bajos) para todos los síntomas")
        void buildsValidKeyForEverySymptom(CycleSymptom symptom) {
            when(messages.resolve(any())).thenReturn("etiqueta");

            service.resolveSymptomLabel(symptom);

            verify(messages).resolve(argThat(key ->
                    key.startsWith("cycle.symptom.") && !key.contains("_")));
        }
    }
}