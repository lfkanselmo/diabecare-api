package com.diabecare.domain.service;

import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseType;
import com.diabecare.support.RealMessageResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExerciseLabelService")
class ExerciseLabelServiceTest {

    private final ExerciseLabelService service = new ExerciseLabelService(RealMessageResolver.spanish());
    private final ExerciseLabelService englishService = new ExerciseLabelService(RealMessageResolver.english());

    @Nested
    @DisplayName("resolveTypeLabel")
    class ResolveTypeLabel {

        @ParameterizedTest(name = "{0} tiene una etiqueta no vacía")
        @EnumSource(ExerciseType.class)
        @DisplayName("resuelve una etiqueta no nula y no vacía para todos los tipos de ejercicio")
        void resolvesNonEmptyLabelForEveryType(ExerciseType type) {
            String label = service.resolveTypeLabel(type);
            assertThat(label).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("resuelve etiquetas específicas correctamente en español")
        void resolvesSpecificLabelsCorrectly() {
            assertThat(service.resolveTypeLabel(ExerciseType.WALKING)).isEqualTo("Caminata");
            assertThat(service.resolveTypeLabel(ExerciseType.SWIMMING)).isEqualTo("Natación");
            assertThat(service.resolveTypeLabel(ExerciseType.OTHER)).isEqualTo("Otro");
        }

        @Test
        @DisplayName("resuelve etiquetas específicas correctamente en inglés")
        void resolvesSpecificLabelsInEnglish() {
            assertThat(englishService.resolveTypeLabel(ExerciseType.WALKING)).isEqualTo("Walking");
            assertThat(englishService.resolveTypeLabel(ExerciseType.SWIMMING)).isEqualTo("Swimming");
            assertThat(englishService.resolveTypeLabel(ExerciseType.OTHER)).isEqualTo("Other");
        }

        @Test
        @DisplayName("cada tipo de ejercicio tiene una etiqueta distinta (sin duplicados accidentales)")
        void allLabelsAreDistinct() {
            var labels = java.util.Arrays.stream(ExerciseType.values())
                    .map(service::resolveTypeLabel)
                    .toList();

            assertThat(labels).doesNotHaveDuplicates();
        }
    }

    @Nested
    @DisplayName("resolveIntensityLabel")
    class ResolveIntensityLabel {

        @ParameterizedTest(name = "{0} tiene una etiqueta no vacía")
        @EnumSource(ExerciseIntensity.class)
        @DisplayName("resuelve una etiqueta no nula y no vacía para todas las intensidades")
        void resolvesNonEmptyLabelForEveryIntensity(ExerciseIntensity intensity) {
            String label = service.resolveIntensityLabel(intensity);
            assertThat(label).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("resuelve las 3 etiquetas de intensidad correctamente")
        void resolvesAllIntensityLabelsCorrectly() {
            assertThat(service.resolveIntensityLabel(ExerciseIntensity.LOW)).isEqualTo("Baja");
            assertThat(service.resolveIntensityLabel(ExerciseIntensity.MODERATE)).isEqualTo("Moderada");
            assertThat(service.resolveIntensityLabel(ExerciseIntensity.HIGH)).isEqualTo("Alta");
        }
    }
}
