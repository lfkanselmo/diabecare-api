package com.diabecare.infrastructure.pdf;

import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.ReportData;
import com.diabecare.domain.model.VitalSign;
import com.diabecare.domain.service.ExerciseLabelService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Generador de PDF con OpenPDF (LGPL/MPL) — migrado desde iText 8 (AGPLv3) para evitar
 * la obligación de liberar el código fuente que exige la edición community de iText.
 */
@Component
@RequiredArgsConstructor
public class MedicalReportPdfGenerator {

    private final MenstrualCycleGuidanceService cycleGuidanceService;
    private final ExerciseLabelService exerciseLabelService;

    private static final Color PRIMARY     = new Color(21, 101, 192);
    private static final Color LIGHT_GRAY  = new Color(245, 247, 250);
    private static final Color SUCCESS     = new Color(46, 125, 50);
    private static final Color WARNING     = new Color(245, 127, 23);
    private static final Color DANGER      = new Color(198, 40, 40);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(ReportData data, LocalDate from, LocalDate to) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            addHeader(doc, data, from, to);
            addPatientInfo(doc, data);
            addGlucoseSummary(doc, data);
            addTirDetailed(doc, data);
            addAverageByReadingType(doc, data);
            addHypoglycemiaEvents(doc, data);
            addAdherence(doc, data);
            addGlucoseHistory(doc, data);
            addTopImpactMeals(doc, data);
            addExerciseSummary(doc, data);
            addVitalSigns(doc, data);
            addMedications(doc, data);
            addMenstrualCycle(doc, data);
            addFooter(doc);
        } catch (DocumentException e) {
            throw new IllegalStateException("Error generando el reporte PDF", e);
        } finally {
            doc.close();
        }

        return baos.toByteArray();
    }

    private void addHeader(Document doc, ReportData data,
                           LocalDate from, LocalDate to) throws DocumentException {
        PdfPTable header = new PdfPTable(new float[]{70, 30});
        header.setWidthPercentage(100);

        PdfPCell titleCell = new PdfPCell();
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.addElement(new Paragraph("DiabeCare", font(24, Font.BOLD, PRIMARY)));
        titleCell.addElement(new Paragraph("Reporte Médico", font(14, Font.NORMAL, Color.DARK_GRAY)));

        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);

        Paragraph periodLabel = new Paragraph("Período", font(9, Font.NORMAL, Color.GRAY));
        periodLabel.setAlignment(Element.ALIGN_RIGHT);
        Paragraph periodValue = new Paragraph(
                from.format(DATE_ONLY) + " al " + to.format(DATE_ONLY), font(10, Font.BOLD, Color.BLACK));
        periodValue.setAlignment(Element.ALIGN_RIGHT);
        Paragraph generated = new Paragraph(
                "Generado: " + LocalDateTime.now().format(DATE_ONLY), font(8, Font.NORMAL, Color.GRAY));
        generated.setAlignment(Element.ALIGN_RIGHT);

        dateCell.addElement(periodLabel);
        dateCell.addElement(periodValue);
        dateCell.addElement(generated);

        header.addCell(titleCell);
        header.addCell(dateCell);
        doc.add(header);

        Paragraph separator = new Paragraph(
                new Chunk(new LineSeparator(2f, 100f, PRIMARY, Element.ALIGN_CENTER, -2f)));
        separator.setSpacingAfter(10f);
        doc.add(separator);
    }

    private void addPatientInfo(Document doc, ReportData data) throws DocumentException {
        doc.add(sectionTitle("Datos del Paciente"));

        PdfPTable table = new PdfPTable(new float[]{25, 25, 25, 25});
        table.setWidthPercentage(100);

        addInfoCell(table, "Nombre", data.getPatient().getFullName());
        addInfoCell(table, "Edad", data.getPatient().getAge() + " años");
        addInfoCell(table, "Tipo de diabetes", data.getPatient().getDiabetesType().name()
                .replace("_", " "));
        addInfoCell(table, "Rango objetivo",
                data.getPatient().getTargetGlucoseMin() + " - " +
                        data.getPatient().getTargetGlucoseMax() + " mg/dL");

        doc.add(table);
        addSpacer(doc);
    }

    private void addGlucoseSummary(Document doc, ReportData data) throws DocumentException {
        doc.add(sectionTitle("Resumen Glucémico"));

        PdfPTable table = new PdfPTable(new float[]{25, 25, 25, 25});
        table.setWidthPercentage(100);

        BigDecimal avg = data.getAverageGlucose();
        BigDecimal hba1c = data.getEstimatedHba1c();
        BigDecimal tir = data.getTimeInRangePercent();
        BigDecimal cv = data.getCoefficientOfVariation();

        addMetricCell(table, "Glucosa promedio",
                avg != null ? avg + " mg/dL" : "Sin datos",
                avg != null && avg.doubleValue() <= 154 ? SUCCESS : WARNING);

        addMetricCell(table, "HbA1c estimada",
                hba1c != null ? hba1c + "%" : "Sin datos",
                hba1c != null && hba1c.doubleValue() <= 7.0 ? SUCCESS : WARNING);

        addMetricCell(table, "Tiempo en rango",
                tir != null ? tir + "%" : "Sin datos",
                tir != null && tir.doubleValue() >= 70 ? SUCCESS : WARNING);

        addMetricCell(table, "Coef. variación",
                cv != null ? cv + "%" : "Sin datos",
                cv != null && cv.doubleValue() < 36 ? SUCCESS : WARNING);

        doc.add(table);
        addSpacer(doc);
    }

    private void addGlucoseHistory(Document doc, ReportData data) throws DocumentException {
        if (data.getGlucoseReadings().isEmpty()) return;

        doc.add(sectionTitle("Historial de Glucosa (últimas 20 lecturas)"));

        PdfPTable table = new PdfPTable(new float[]{30, 20, 25, 25});
        table.setWidthPercentage(100);

        addTableHeader(table, "Fecha y hora", "Valor", "Tipo", "Estado");

        for (var r : data.getGlucoseReadings().stream().limit(20).toList()) {
            table.addCell(bodyCell(r.getMeasuredAt().format(DATE_FMT)));
            table.addCell(bodyCell(r.getValueInMgDl() + " mg/dL"));
            table.addCell(bodyCell(formatReadingType(r.getReadingType().name())));

            String statusLabel = formatStatus(r.getStatus().name());
            Color statusColor = getStatusColor(r.getStatus().name());
            PdfPCell statusCell = new PdfPCell(new Paragraph(statusLabel, font(9, Font.NORMAL, statusColor)));
            statusCell.setBackgroundColor(LIGHT_GRAY);
            statusCell.setPadding(4f);
            table.addCell(statusCell);
        }

        doc.add(table);
        addSpacer(doc);
    }

    private void addVitalSigns(Document doc, ReportData data) throws DocumentException {
        if (data.getVitalSigns().isEmpty()) return;

        doc.add(sectionTitle("Último Registro de Signos Vitales"));

        VitalSign latest = data.getVitalSigns().get(0);
        PdfPTable table = new PdfPTable(new float[]{25, 25, 25, 25});
        table.setWidthPercentage(100);

        if (latest.getWeightKg() != null)
            addInfoCell(table, "Peso", latest.getWeightKg() + " kg");
        if (latest.calculateBmi() != null)
            addInfoCell(table, "IMC", latest.calculateBmi() + " (" +
                    (latest.getBmiCategory() != null ? latest.getBmiCategory().name() : "-") + ")");
        if (latest.getSystolicBp() != null)
            addInfoCell(table, "Presión arterial",
                    latest.getSystolicBp() + "/" + latest.getDiastolicBp() + " mmHg");
        if (latest.getHba1c() != null)
            addInfoCell(table, "HbA1c medida", latest.getHba1c() + "%");

        doc.add(table);
        addSpacer(doc);
    }

    private void addMedications(Document doc, ReportData data) throws DocumentException {
        if (data.getMedications().isEmpty()) return;

        doc.add(sectionTitle("Medicamentos Activos"));

        PdfPTable table = new PdfPTable(new float[]{35, 20, 20, 25});
        table.setWidthPercentage(100);

        addTableHeader(table, "Medicamento", "Tipo", "Dosis", "Frecuencia");

        for (var m : data.getMedications()) {
            table.addCell(bodyCell(m.getName()));
            table.addCell(bodyCell(m.getType().name().replace("_", " ")));
            table.addCell(bodyCell(m.getDose() + " " + m.getDoseUnit().name()));
            table.addCell(bodyCell(formatFrequency(m.getFrequency().name())));
        }

        doc.add(table);
    }

    private void addFooter(Document doc) throws DocumentException {
        addSpacer(doc);

        Paragraph separator = new Paragraph(
                new Chunk(new LineSeparator(1f, 100f, Color.LIGHT_GRAY, Element.ALIGN_CENTER, -2f)));
        doc.add(separator);

        Paragraph footerText = new Paragraph(
                "Este reporte fue generado por DiabeCare. " +
                        "Los valores de HbA1c son estimados mediante la fórmula ADAG y no reemplazan " +
                        "el análisis de laboratorio. Consulte siempre a su médico tratante.",
                font(8, Font.NORMAL, Color.GRAY));
        footerText.setAlignment(Element.ALIGN_CENTER);
        doc.add(footerText);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Font font(float size, int style, Color color) {
        return new Font(Font.HELVETICA, size, style, color);
    }

    private void addSpacer(Document doc) throws DocumentException {
        Paragraph spacer = new Paragraph(" ", font(4, Font.NORMAL, Color.BLACK));
        doc.add(spacer);
    }

    private Paragraph sectionTitle(String title) {
        Paragraph p = new Paragraph(title, font(13, Font.BOLD, PRIMARY));
        p.setSpacingBefore(8f);
        p.setSpacingAfter(6f);
        return p;
    }

    private void addInfoCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setPadding(6f);
        cell.addElement(new Paragraph(label, font(8, Font.NORMAL, Color.GRAY)));
        cell.addElement(new Paragraph(value, font(10, Font.BOLD, Color.BLACK)));
        table.addCell(cell);
    }

    private void addMetricCell(PdfPTable table, String label, String value, Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setPadding(8f);
        cell.addElement(new Paragraph(label, font(8, Font.NORMAL, Color.GRAY)));
        cell.addElement(new Paragraph(value, font(14, Font.BOLD, color)));
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(h, font(9, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(PRIMARY);
            cell.setPadding(5f);
            table.addCell(cell);
        }
    }

    private PdfPCell bodyCell(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font(9, Font.NORMAL, Color.BLACK)));
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setPadding(4f);
        return cell;
    }

    private String formatReadingType(String type) {
        return switch (type) {
            case "FASTING"   -> "Ayuno";
            case "PRE_MEAL"  -> "Preprandial";
            case "POST_MEAL" -> "Postprandial";
            case "BEDTIME"   -> "Antes de dormir";
            default          -> "Aleatoria";
        };
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "CRITICALLY_LOW"  -> "Crítico bajo";
            case "LOW"             -> "Bajo";
            case "NORMAL"          -> "Normal";
            case "HIGH"            -> "Alto";
            case "CRITICALLY_HIGH" -> "Crítico alto";
            default                -> status;
        };
    }

    private String formatFrequency(String freq) {
        return switch (freq) {
            case "ONCE_DAILY"        -> "Una vez al día";
            case "TWICE_DAILY"       -> "Dos veces al día";
            case "THREE_TIMES_DAILY" -> "Tres veces al día";
            case "WITH_MEALS"        -> "Con las comidas";
            case "BEFORE_MEALS"      -> "Antes de comidas";
            case "AT_BEDTIME"        -> "Al acostarse";
            default                  -> "Según necesidad";
        };
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case "NORMAL"          -> SUCCESS;
            case "HIGH", "LOW"     -> WARNING;
            default                -> DANGER;
        };
    }

    private void addTirDetailed(Document doc, ReportData data) throws DocumentException {
        if (data.getTirDetailed().isEmpty()) return;

        doc.add(sectionTitle("Distribución del Tiempo en Rango (Consenso Internacional)"));

        PdfPTable table = new PdfPTable(new float[]{40, 20, 40});
        table.setWidthPercentage(100);

        addTableHeader(table, "Rango", "Porcentaje", "Objetivo ADA");

        Map<String, String[]> ranges = new java.util.LinkedHashMap<>();
        ranges.put("veryLow",  new String[]{"Muy bajo (<54 mg/dL)",   "<1%"  });
        ranges.put("low",      new String[]{"Bajo (54-70 mg/dL)",     "<4%"  });
        ranges.put("inRange",  new String[]{"En rango (70-180 mg/dL)","≥70%" });
        ranges.put("high",     new String[]{"Alto (180-250 mg/dL)",   "<25%" });
        ranges.put("veryHigh", new String[]{"Muy alto (>250 mg/dL)",  "<5%"  });

        Map<String, Color> rangeColors = Map.of(
                "veryLow",  DANGER,
                "low",      new Color(255, 152, 0),
                "inRange",  SUCCESS,
                "high",     new Color(255, 152, 0),
                "veryHigh", DANGER
        );

        for (var entry : data.getTirDetailed().entrySet()) {
            String key = entry.getKey();
            if (!ranges.containsKey(key)) continue;

            String[] info = ranges.get(key);
            Color color = rangeColors.getOrDefault(key, SUCCESS);
            table.addCell(bodyCell(info[0]));

            PdfPCell valueCell = new PdfPCell(new Paragraph(entry.getValue() + "%", font(10, Font.BOLD, color)));
            valueCell.setBackgroundColor(LIGHT_GRAY);
            valueCell.setPadding(4f);
            table.addCell(valueCell);

            table.addCell(bodyCell(info[1]));
        }

        doc.add(table);
        addSpacer(doc);
    }

    private void addAverageByReadingType(Document doc, ReportData data) throws DocumentException {
        if (data.getAverageByReadingType().isEmpty()) return;

        doc.add(sectionTitle("Promedio de Glucosa por Período del Día"));

        PdfPTable table = new PdfPTable(new float[]{50, 50});
        table.setWidthPercentage(100);

        addTableHeader(table, "Tipo de lectura", "Promedio (mg/dL)");

        Map<String, String> typeLabels = Map.of(
                "FASTING",   "Ayuno",
                "PRE_MEAL",  "Preprandial",
                "POST_MEAL", "Postprandial",
                "BEDTIME",   "Antes de dormir",
                "RANDOM",    "Aleatoria"
        );

        for (var entry : data.getAverageByReadingType().entrySet()) {
            String type = entry.getKey();
            BigDecimal avg = entry.getValue();
            table.addCell(bodyCell(typeLabels.getOrDefault(type, type)));

            Color color = avg.doubleValue() > 180 ? WARNING :
                    avg.doubleValue() < 70  ? DANGER  : SUCCESS;
            PdfPCell cell = new PdfPCell(new Paragraph(avg + " mg/dL", font(10, Font.BOLD, color)));
            cell.setBackgroundColor(LIGHT_GRAY);
            cell.setPadding(4f);
            table.addCell(cell);
        }

        doc.add(table);
        addSpacer(doc);
    }

    private void addHypoglycemiaEvents(Document doc, ReportData data) throws DocumentException {
        doc.add(sectionTitle("Episodios de Hipoglucemia (<70 mg/dL)"));

        if (data.getHypoglycemiaEvents().isEmpty()) {
            Paragraph ok = new Paragraph(
                    "Sin episodios de hipoglucemia en el período.", font(10, Font.NORMAL, SUCCESS));
            ok.setSpacingAfter(8f);
            doc.add(ok);
            return;
        }

        Paragraph warning = new Paragraph(
                "Se registraron " + data.getHypoglycemiaEvents().size() +
                        " episodios de hipoglucemia.", font(10, Font.NORMAL, DANGER));
        warning.setSpacingAfter(6f);
        doc.add(warning);

        PdfPTable table = new PdfPTable(new float[]{35, 20, 25, 20});
        table.setWidthPercentage(100);

        addTableHeader(table, "Fecha y hora", "Valor", "Tipo", "Estado");

        for (var r : data.getHypoglycemiaEvents().stream().limit(10).toList()) {
            table.addCell(bodyCell(r.getMeasuredAt().format(DATE_FMT)));

            PdfPCell valueCell = new PdfPCell(
                    new Paragraph(r.getValueInMgDl() + " mg/dL", font(9, Font.BOLD, DANGER)));
            valueCell.setBackgroundColor(LIGHT_GRAY);
            valueCell.setPadding(4f);
            table.addCell(valueCell);

            table.addCell(bodyCell(formatReadingType(r.getReadingType().name())));
            table.addCell(bodyCell(formatStatus(r.getStatus().name())));
        }

        doc.add(table);
        addSpacer(doc);
    }

    private void addAdherence(Document doc, ReportData data) throws DocumentException {
        doc.add(sectionTitle("Adherencia al Monitoreo"));

        double adherence = data.getAdherencePercent();
        Color color   = adherence >= 80 ? SUCCESS : adherence >= 50 ? WARNING : DANGER;
        String label  = adherence >= 80 ? "Buena adherencia" :
                adherence >= 50 ? "Adherencia moderada" : "Baja adherencia";

        PdfPTable table = new PdfPTable(new float[]{33, 33, 33});
        table.setWidthPercentage(100);

        addInfoCell(table, "Días con registro",
                data.getGlucoseReadings().stream()
                        .map(r -> r.getMeasuredAt().toLocalDate())
                        .distinct().count() + " días");

        addInfoCell(table, "Total de lecturas",
                data.getGlucoseReadings().size() + " lecturas");

        PdfPCell adherenceCell = new PdfPCell();
        adherenceCell.setBorder(Rectangle.NO_BORDER);
        adherenceCell.setBackgroundColor(LIGHT_GRAY);
        adherenceCell.setPadding(6f);
        adherenceCell.addElement(new Paragraph("Adherencia", font(8, Font.NORMAL, Color.GRAY)));
        adherenceCell.addElement(new Paragraph(String.format("%.1f%%", adherence), font(14, Font.BOLD, color)));
        adherenceCell.addElement(new Paragraph(label, font(8, Font.NORMAL, color)));
        table.addCell(adherenceCell);

        doc.add(table);
        addSpacer(doc);
    }

    private void addTopImpactMeals(Document doc, ReportData data) throws DocumentException {
        if (data.getTopImpactMeals().isEmpty()) return;

        doc.add(sectionTitle("Comidas con Mayor Impacto Calórico"));

        PdfPTable table = new PdfPTable(new float[]{30, 17, 17, 17, 19});
        table.setWidthPercentage(100);

        addTableHeader(table, "Fecha", "Tipo", "Calorías", "Carbohidratos", "Proteínas");

        for (var m : data.getTopImpactMeals()) {
            table.addCell(bodyCell(m.getConsumedAt().format(DATE_FMT)));
            table.addCell(bodyCell(formatMealType(m.getMealType().name())));
            table.addCell(bodyCell(m.getTotalCalories() + " kcal"));
            table.addCell(bodyCell(m.getTotalCarbohydrates() + " g"));
            table.addCell(bodyCell(m.getTotalProteins() + " g"));
        }

        doc.add(table);
        addSpacer(doc);
    }

    private void addExerciseSummary(Document doc, ReportData data) throws DocumentException {
        if (data.getExerciseLogs().isEmpty()) return;

        doc.add(sectionTitle("Actividad Física en el Período"));

        long totalSessions = data.getExerciseLogs().size();
        int  totalMinutes  = data.getExerciseLogs().stream()
                .mapToInt(ExerciseLog::getDurationMinutes).sum();
        double totalCalories = data.getExerciseLogs().stream()
                .mapToDouble(e -> e.getCaloriesBurned() != null ?
                        e.getCaloriesBurned().doubleValue() : 0)
                .sum();

        PdfPTable summary = new PdfPTable(new float[]{33, 33, 33});
        summary.setWidthPercentage(100);

        addInfoCell(summary, "Sesiones totales", totalSessions + " sesiones");
        addInfoCell(summary, "Tiempo total", totalMinutes + " minutos");
        addInfoCell(summary, "Calorías quemadas", String.format("%.0f kcal", totalCalories));

        doc.add(summary);
        addSpacer(doc);

        PdfPTable detail = new PdfPTable(new float[]{30, 25, 20, 25});
        detail.setWidthPercentage(100);

        addTableHeader(detail, "Fecha", "Ejercicio", "Duración", "Intensidad");

        for (var e : data.getExerciseLogs().stream().limit(10).toList()) {
            detail.addCell(bodyCell(e.getPerformedAt().format(DATE_FMT)));
            detail.addCell(bodyCell(exerciseLabelService.resolveTypeLabel(e.getExerciseType())));
            detail.addCell(bodyCell(e.getDurationMinutes() + " min"));
            detail.addCell(bodyCell(exerciseLabelService.resolveIntensityLabel(e.getIntensity())));
        }

        doc.add(detail);
        addSpacer(doc);
    }

    private void addMenstrualCycle(Document doc, ReportData data) throws DocumentException {
        if (data.getLatestMenstrualCycle() == null) return;

        doc.add(sectionTitle("Ciclo Menstrual"));

        MenstrualCycle cycle = data.getLatestMenstrualCycle();
        LocalDate today = LocalDate.now();
        var currentPhase = cycle.calculateCurrentPhase(
                today, data.getAverageCycleLength(), data.getAveragePeriodLength());
        PdfPTable table = new PdfPTable(new float[]{25, 25, 25, 25});
        table.setWidthPercentage(100);

        addInfoCell(table, "Inicio del ciclo",
                cycle.getStartDate().format(DATE_ONLY));
        addInfoCell(table, "Fase actual",
                cycleGuidanceService.resolveLabel(currentPhase));
        addInfoCell(table, "Próximo ciclo",
                cycle.predictNextCycleStart(today, data.getAverageCycleLength()).format(DATE_ONLY));
        addInfoCell(table, "Duración promedio",
                (data.getAverageCycleLength() != null ? data.getAverageCycleLength() : 28) + " días");

        doc.add(table);

        PdfPCell guidanceCell = new PdfPCell(
                new Paragraph(cycleGuidanceService.resolveGuidance(currentPhase),
                        font(9, Font.NORMAL, Color.DARK_GRAY)));
        guidanceCell.setBorder(Rectangle.NO_BORDER);
        guidanceCell.setBackgroundColor(LIGHT_GRAY);
        guidanceCell.setPadding(8f);

        PdfPTable guidanceTable = new PdfPTable(new float[]{100});
        guidanceTable.setWidthPercentage(100);
        guidanceTable.setSpacingBefore(6f);
        guidanceTable.addCell(guidanceCell);
        doc.add(guidanceTable);
    }

    // ── Helpers adicionales ───────────────────────────────────────────────────

    private String formatMealType(String type) {
        return switch (type) {
            case "BREAKFAST" -> "Desayuno";
            case "LUNCH"     -> "Almuerzo";
            case "DINNER"    -> "Cena";
            case "SNACK"     -> "Merienda";
            default          -> type;
        };
    }
}
