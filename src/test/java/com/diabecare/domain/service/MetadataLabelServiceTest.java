package com.diabecare.domain.service;

import com.diabecare.domain.model.*;
import com.diabecare.support.RealMessageResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MetadataLabelService")
class MetadataLabelServiceTest {

    private final MetadataLabelService service = new MetadataLabelService(RealMessageResolver.spanish());
    private final MetadataLabelService englishService = new MetadataLabelService(RealMessageResolver.english());

    @Nested
    @DisplayName("resolveMedicationTypeLabel")
    class ResolveMedicationTypeLabel {

        @ParameterizedTest
        @EnumSource(MedicationType.class)
        @DisplayName("resuelve una etiqueta no vacía para todos los tipos")
        void resolvesNonBlankLabel(MedicationType type) {
            assertThat(service.resolveMedicationTypeLabel(type)).isNotBlank();
        }

        @Test
        @DisplayName("resuelve correctamente en español e inglés")
        void resolvesInBothLocales() {
            assertThat(service.resolveMedicationTypeLabel(MedicationType.ORAL)).isEqualTo("Medicamento oral");
            assertThat(englishService.resolveMedicationTypeLabel(MedicationType.ORAL)).isEqualTo("Oral medication");
        }
    }

    @Nested
    @DisplayName("resolveDoseUnitLabel")
    class ResolveDoseUnitLabel {

        @ParameterizedTest
        @EnumSource(DoseUnit.class)
        @DisplayName("resuelve una etiqueta no vacía para todas las unidades")
        void resolvesNonBlankLabel(DoseUnit unit) {
            assertThat(service.resolveDoseUnitLabel(unit)).isNotBlank();
        }

        @Test
        @DisplayName("resuelve mg correctamente")
        void resolvesMg() {
            assertThat(service.resolveDoseUnitLabel(DoseUnit.MG)).isEqualTo("mg");
        }
    }

    @Nested
    @DisplayName("resolveFrequencyLabel")
    class ResolveFrequencyLabel {

        @ParameterizedTest
        @EnumSource(MedicationFrequency.class)
        @DisplayName("resuelve una etiqueta no vacía para todas las frecuencias")
        void resolvesNonBlankLabel(MedicationFrequency frequency) {
            assertThat(service.resolveFrequencyLabel(frequency)).isNotBlank();
        }

        @Test
        @DisplayName("resuelve correctamente en español e inglés")
        void resolvesInBothLocales() {
            assertThat(service.resolveFrequencyLabel(MedicationFrequency.ONCE_DAILY)).isEqualTo("Una vez al día");
            assertThat(englishService.resolveFrequencyLabel(MedicationFrequency.ONCE_DAILY)).isEqualTo("Once daily");
        }
    }

    @Nested
    @DisplayName("resolveMealTypeLabel")
    class ResolveMealTypeLabel {

        @ParameterizedTest
        @EnumSource(MealType.class)
        @DisplayName("resuelve una etiqueta no vacía para todos los tipos de comida")
        void resolvesNonBlankLabel(MealType type) {
            assertThat(service.resolveMealTypeLabel(type)).isNotBlank();
        }

        @Test
        @DisplayName("resuelve correctamente en español e inglés")
        void resolvesInBothLocales() {
            assertThat(service.resolveMealTypeLabel(MealType.BREAKFAST)).isEqualTo("Desayuno");
            assertThat(englishService.resolveMealTypeLabel(MealType.BREAKFAST)).isEqualTo("Breakfast");
        }
    }

    @Nested
    @DisplayName("resolveReadingTypeLabel")
    class ResolveReadingTypeLabel {

        @ParameterizedTest
        @EnumSource(ReadingType.class)
        @DisplayName("resuelve una etiqueta no vacía para todos los tipos de lectura")
        void resolvesNonBlankLabel(ReadingType type) {
            assertThat(service.resolveReadingTypeLabel(type)).isNotBlank();
        }

        @Test
        @DisplayName("resuelve fasting correctamente")
        void resolvesFasting() {
            assertThat(service.resolveReadingTypeLabel(ReadingType.FASTING)).isEqualTo("Ayuno");
        }
    }

    @Nested
    @DisplayName("resolveActivityLevelLabel")
    class ResolveActivityLevelLabel {

        @ParameterizedTest
        @EnumSource(ActivityLevel.class)
        @DisplayName("resuelve una etiqueta no vacía para todos los niveles")
        void resolvesNonBlankLabel(ActivityLevel level) {
            assertThat(service.resolveActivityLevelLabel(level)).isNotBlank();
        }
    }

    @Nested
    @DisplayName("resolveDiabetesTypeLabel")
    class ResolveDiabetesTypeLabel {

        @ParameterizedTest
        @EnumSource(DiabetesType.class)
        @DisplayName("resuelve una etiqueta no vacía para todos los tipos")
        void resolvesNonBlankLabel(DiabetesType type) {
            assertThat(service.resolveDiabetesTypeLabel(type)).isNotBlank();
        }

        @Test
        @DisplayName("resuelve tipo 1 correctamente")
        void resolvesType1() {
            assertThat(service.resolveDiabetesTypeLabel(DiabetesType.TYPE_1)).isEqualTo("Tipo 1");
        }
    }

    @Nested
    @DisplayName("resolveGlucoseUnitLabel")
    class ResolveGlucoseUnitLabel {

        @ParameterizedTest
        @EnumSource(GlucoseUnit.class)
        @DisplayName("resuelve una etiqueta no vacía para todas las unidades")
        void resolvesNonBlankLabel(GlucoseUnit unit) {
            assertThat(service.resolveGlucoseUnitLabel(unit)).isNotBlank();
        }
    }

    @Nested
    @DisplayName("resolveGlucoseStatusLabel")
    class ResolveGlucoseStatusLabel {

        @ParameterizedTest
        @EnumSource(GlucoseStatus.class)
        @DisplayName("resuelve una etiqueta no vacía para todos los estados")
        void resolvesNonBlankLabel(GlucoseStatus status) {
            assertThat(service.resolveGlucoseStatusLabel(status)).isNotBlank();
        }

        @Test
        @DisplayName("resuelve correctamente en español e inglés")
        void resolvesInBothLocales() {
            assertThat(service.resolveGlucoseStatusLabel(GlucoseStatus.CRITICALLY_LOW)).isEqualTo("Crítico bajo");
            assertThat(englishService.resolveGlucoseStatusLabel(GlucoseStatus.CRITICALLY_LOW)).isEqualTo("Critically low");
        }
    }

    @Nested
    @DisplayName("resolveSeverityLabel")
    class ResolveSeverityLabel {

        @ParameterizedTest
        @EnumSource(SymptomSeverity.class)
        @DisplayName("resuelve una etiqueta no vacía para todas las severidades")
        void resolvesNonBlankLabel(SymptomSeverity severity) {
            assertThat(service.resolveSeverityLabel(severity)).isNotBlank();
        }
    }
}
