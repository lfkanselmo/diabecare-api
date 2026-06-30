package com.diabecare.domain.service;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GlucoseExportService")
class GlucoseExportServiceTest {

    private final GlucoseExportService service = new GlucoseExportService();
    private final UUID patientId = UUID.randomUUID();

    @Nested
    @DisplayName("toCsv")
    class ToCsv {

        @Test
        @DisplayName("incluye el BOM UTF-8 al inicio para que Excel lea tildes correctamente")
        void includesUtf8Bom() {
            String csv = service.toCsv(List.of());
            assertThat(csv).startsWith("\uFEFF");
        }

        @Test
        @DisplayName("incluye la cabecera correcta")
        void includesCorrectHeader() {
            String csv = service.toCsv(List.of());
            assertThat(csv).contains("id,valor,unidad,tipo,estado,fecha,notas");
        }

        @Test
        @DisplayName("genera una fila por cada lectura con sus datos correctos")
        void generatesOneRowPerReading() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(100), "sin novedad");

            String csv = service.toCsv(List.of(reading));

            assertThat(csv).contains(reading.getReadingId().toString());
            assertThat(csv).contains("100");
            assertThat(csv).contains("MG_DL");
            assertThat(csv).contains("RANDOM");
            assertThat(csv).contains("NORMAL");
            assertThat(csv).contains("sin novedad");
        }

        @Test
        @DisplayName("escapa las comas dentro de las notas para no romper el formato CSV")
        void escapesCommasInNotes() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(100), "después de almorzar, con estrés");

            String csv = service.toCsv(List.of(reading));

            assertThat(csv).contains("después de almorzar; con estrés");
            assertThat(csv).doesNotContain("después de almorzar, con estrés");
        }

        @Test
        @DisplayName("usa cadena vacía cuando las notas son nulas, sin escribir la palabra null")
        void usesEmptyStringWhenNotesAreNull() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(100), null);

            String csv = service.toCsv(List.of(reading));
            String dataLine = csv.split("\n")[1];

            assertThat(dataLine).endsWith(",");
            assertThat(dataLine.toLowerCase()).doesNotContain("null");
        }

        @Test
        @DisplayName("genera solo cabecera con BOM cuando la lista está vacía")
        void generatesOnlyHeaderWhenEmpty() {
            String csv = service.toCsv(List.of());
            String[] lines = csv.replace("\uFEFF", "").split("\n");
            assertThat(lines).hasSize(1);
        }
    }

    @Nested
    @DisplayName("toJson")
    class ToJson {

        @Test
        @DisplayName("serializa una lista de lecturas a JSON válido")
        void serializesReadingsToJson() {
            GlucoseReading reading = readingWith(BigDecimal.valueOf(120), "nota");

            String json = service.toJson(List.of(reading));

            assertThat(json).contains("120");
            assertThat(json).contains(reading.getReadingId().toString());
        }

        @Test
        @DisplayName("serializa una lista vacía como un arreglo JSON vacío")
        void serializesEmptyListAsEmptyArray() {
            String json = service.toJson(List.of());
            assertThat(json.trim()).isEqualTo("[]");
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private GlucoseReading readingWith(BigDecimal value, String notes) {
        return GlucoseReading.builder()
                .readingId(UUID.randomUUID())
                .patientId(patientId)
                .value(value)
                .unit(GlucoseUnit.MG_DL)
                .readingType(ReadingType.RANDOM)
                .measuredAt(LocalDateTime.of(2026, 6, 1, 8, 0))
                .notes(notes)
                .build();
    }
}