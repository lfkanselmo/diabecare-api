package com.diabecare.infrastructure.pdf;

import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.domain.model.ReportData;
import com.diabecare.domain.model.VitalSign;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MedicalReportPdfGenerator {

    private final MenstrualCycleGuidanceService cycleGuidanceService;

    private static final DeviceRgb PRIMARY     = new DeviceRgb(21, 101, 192);
    private static final DeviceRgb LIGHT_GRAY  = new DeviceRgb(245, 247, 250);
    private static final DeviceRgb SUCCESS     = new DeviceRgb(46, 125, 50);
    private static final DeviceRgb WARNING     = new DeviceRgb(245, 127, 23);
    private static final DeviceRgb DANGER      = new DeviceRgb(198, 40, 40);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(ReportData data, LocalDate from, LocalDate to) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);
        doc.setMargins(36, 36, 36, 36);

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

        doc.close();
        return baos.toByteArray();
    }

    private void addHeader(Document doc, ReportData data,
                           LocalDate from, LocalDate to) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                .setWidth(UnitValue.createPercentValue(100));

        Cell titleCell = new Cell()
                .add(new Paragraph("DiabeCare")
                        .setFontSize(24).setBold().setFontColor(PRIMARY))
                .add(new Paragraph("Reporte Médico")
                        .setFontSize(14).setFontColor(ColorConstants.DARK_GRAY))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        Cell dateCell = new Cell()
                .add(new Paragraph("Período")
                        .setFontSize(9).setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(from.format(DATE_ONLY) + " al " + to.format(DATE_ONLY))
                        .setFontSize(10).setBold()
                        .setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph("Generado: " + LocalDateTime.now().format(DATE_ONLY))
                        .setFontSize(8).setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);

        header.addCell(titleCell);
        header.addCell(dateCell);
        doc.add(header);
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(2f))
                .setStrokeColor(PRIMARY).setMarginBottom(10));
    }

    private void addPatientInfo(Document doc, ReportData data) {
        doc.add(sectionTitle("Datos del Paciente"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100));

        addInfoCell(table, "Nombre", data.getPatient().getFullName());
        addInfoCell(table, "Edad", data.getPatient().getAge() + " años");
        addInfoCell(table, "Tipo de diabetes", data.getPatient().getDiabetesType().name()
                .replace("_", " "));
        addInfoCell(table, "Rango objetivo",
                data.getPatient().getTargetGlucoseMin() + " - " +
                        data.getPatient().getTargetGlucoseMax() + " mg/dL");

        doc.add(table);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addGlucoseSummary(Document doc, ReportData data) {
        doc.add(sectionTitle("Resumen Glucémico"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100));

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
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addGlucoseHistory(Document doc, ReportData data) {
        if (data.getGlucoseReadings().isEmpty()) return;

        doc.add(sectionTitle("Historial de Glucosa (últimas 20 lecturas)"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 20, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableHeader(table, "Fecha y hora", "Valor", "Tipo", "Estado");

        data.getGlucoseReadings().stream().limit(20).forEach(r -> {
            table.addCell(bodyCell(r.getMeasuredAt().format(DATE_FMT)));
            table.addCell(bodyCell(r.getValueInMgDl() + " mg/dL"));
            table.addCell(bodyCell(formatReadingType(r.getReadingType().name())));

            String statusLabel = formatStatus(r.getStatus().name());
            DeviceRgb statusColor = getStatusColor(r.getStatus().name());
            table.addCell(new Cell()
                    .add(new Paragraph(statusLabel).setFontSize(9).setFontColor(statusColor))
                    .setBackgroundColor(LIGHT_GRAY)
                    .setPadding(4));
        });

        doc.add(table);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addVitalSigns(Document doc, ReportData data) {
        if (data.getVitalSigns().isEmpty()) return;

        doc.add(sectionTitle("Último Registro de Signos Vitales"));

        VitalSign latest = data.getVitalSigns().get(0);
        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100));

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
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addMedications(Document doc, ReportData data) {
        if (data.getMedications().isEmpty()) return;

        doc.add(sectionTitle("Medicamentos Activos"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 20, 20, 25}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableHeader(table, "Medicamento", "Tipo", "Dosis", "Frecuencia");

        data.getMedications().forEach(m -> {
            table.addCell(bodyCell(m.getName()));
            table.addCell(bodyCell(m.getType().name().replace("_", " ")));
            table.addCell(bodyCell(m.getDose() + " " + m.getDoseUnit().name()));
            table.addCell(bodyCell(formatFrequency(m.getFrequency().name())));
        });

        doc.add(table);
    }

    private void addFooter(Document doc) {
        doc.add(new Paragraph("\n").setFontSize(8));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
                .setStrokeColor(ColorConstants.LIGHT_GRAY));
        doc.add(new Paragraph(
                "Este reporte fue generado por DiabeCare. " +
                        "Los valores de HbA1c son estimados mediante la fórmula ADAG y no reemplazan " +
                        "el análisis de laboratorio. Consulte siempre a su médico tratante.")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
    }

    private Paragraph sectionTitle(String title) {
        return new Paragraph(title)
                .setFontSize(13).setBold().setFontColor(PRIMARY)
                .setMarginTop(8).setMarginBottom(6);
    }

    private void addInfoCell(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFontSize(8).setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(value).setFontSize(10).setBold())
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(6)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
    }

    private void addMetricCell(Table table, String label, String value, DeviceRgb color) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFontSize(8).setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(value).setFontSize(14).setBold().setFontColor(color))
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(8)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
    }

    private void addTableHeader(Table table, String... headers) {
        for (String h : headers) {
            table.addCell(new Cell()
                    .add(new Paragraph(h).setFontSize(9).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY)
                    .setPadding(5));
        }
    }

    private Cell bodyCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setFontSize(9))
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(4);
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

    private DeviceRgb getStatusColor(String status) {
        return switch (status) {
            case "NORMAL"          -> SUCCESS;
            case "HIGH", "LOW"     -> WARNING;
            default                -> DANGER;
        };
    }

    private void addTirDetailed(Document doc, ReportData data) {
        if (data.getTirDetailed().isEmpty()) return;

        doc.add(sectionTitle("Distribución del Tiempo en Rango (Consenso Internacional)"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{40, 20, 40}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableHeader(table, "Rango", "Porcentaje", "Objetivo ADA");

        Map<String, String[]> ranges = new java.util.LinkedHashMap<>();
        ranges.put("veryLow",  new String[]{"Muy bajo (<54 mg/dL)",   "<1%"  });
        ranges.put("low",      new String[]{"Bajo (54-70 mg/dL)",     "<4%"  });
        ranges.put("inRange",  new String[]{"En rango (70-180 mg/dL)","≥70%" });
        ranges.put("high",     new String[]{"Alto (180-250 mg/dL)",   "<25%" });
        ranges.put("veryHigh", new String[]{"Muy alto (>250 mg/dL)",  "<5%"  });

        Map<String, DeviceRgb> rangeColors = Map.of(
                "veryLow",  DANGER,
                "low",      new DeviceRgb(255, 152, 0),
                "inRange",  SUCCESS,
                "high",     new DeviceRgb(255, 152, 0),
                "veryHigh", DANGER
        );

        data.getTirDetailed().forEach((key, value) -> {
            if (ranges.containsKey(key)) {
                String[] info = ranges.get(key);
                DeviceRgb color = rangeColors.getOrDefault(key, SUCCESS);
                table.addCell(bodyCell(info[0]));
                table.addCell(new Cell()
                        .add(new Paragraph(value + "%").setFontSize(10).setBold().setFontColor(color))
                        .setBackgroundColor(LIGHT_GRAY).setPadding(4));
                table.addCell(bodyCell(info[1]));
            }
        });

        doc.add(table);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addAverageByReadingType(Document doc, ReportData data) {
        if (data.getAverageByReadingType().isEmpty()) return;

        doc.add(sectionTitle("Promedio de Glucosa por Período del Día"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableHeader(table, "Tipo de lectura", "Promedio (mg/dL)");

        Map<String, String> typeLabels = Map.of(
                "FASTING",   "Ayuno",
                "PRE_MEAL",  "Preprandial",
                "POST_MEAL", "Postprandial",
                "BEDTIME",   "Antes de dormir",
                "RANDOM",    "Aleatoria"
        );

        data.getAverageByReadingType().forEach((type, avg) -> {
            table.addCell(bodyCell(typeLabels.getOrDefault(type, type)));
            DeviceRgb color = avg.doubleValue() > 180 ? WARNING :
                    avg.doubleValue() < 70  ? DANGER  : SUCCESS;
            table.addCell(new Cell()
                    .add(new Paragraph(avg + " mg/dL").setFontSize(10).setBold().setFontColor(color))
                    .setBackgroundColor(LIGHT_GRAY).setPadding(4));
        });

        doc.add(table);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addHypoglycemiaEvents(Document doc, ReportData data) {
        doc.add(sectionTitle("Episodios de Hipoglucemia (<70 mg/dL)"));

        if (data.getHypoglycemiaEvents().isEmpty()) {
            doc.add(new Paragraph("✓ Sin episodios de hipoglucemia en el período.")
                    .setFontSize(10).setFontColor(SUCCESS).setMarginBottom(8));
            return;
        }

        doc.add(new Paragraph("⚠ Se registraron " + data.getHypoglycemiaEvents().size() +
                " episodios de hipoglucemia.")
                .setFontSize(10).setFontColor(DANGER).setMarginBottom(6));

        Table table = new Table(UnitValue.createPercentArray(new float[]{35, 20, 25, 20}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableHeader(table, "Fecha y hora", "Valor", "Tipo", "Estado");

        data.getHypoglycemiaEvents().stream().limit(10).forEach(r -> {
            table.addCell(bodyCell(r.getMeasuredAt().format(DATE_FMT)));
            table.addCell(new Cell()
                    .add(new Paragraph(r.getValueInMgDl() + " mg/dL")
                            .setFontSize(9).setBold().setFontColor(DANGER))
                    .setBackgroundColor(LIGHT_GRAY).setPadding(4));
            table.addCell(bodyCell(formatReadingType(r.getReadingType().name())));
            table.addCell(bodyCell(formatStatus(r.getStatus().name())));
        });

        doc.add(table);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addAdherence(Document doc, ReportData data) {
        doc.add(sectionTitle("Adherencia al Monitoreo"));

        double adherence = data.getAdherencePercent();
        DeviceRgb color  = adherence >= 80 ? SUCCESS : adherence >= 50 ? WARNING : DANGER;
        String label     = adherence >= 80 ? "Buena adherencia" :
                adherence >= 50 ? "Adherencia moderada" : "Baja adherencia";

        Table table = new Table(UnitValue.createPercentArray(new float[]{33, 33, 33}))
                .setWidth(UnitValue.createPercentValue(100));

        addInfoCell(table, "Días con registro",
                data.getGlucoseReadings().stream()
                        .map(r -> r.getMeasuredAt().toLocalDate())
                        .distinct().count() + " días");

        addInfoCell(table, "Total de lecturas",
                data.getGlucoseReadings().size() + " lecturas");

        table.addCell(new Cell()
                .add(new Paragraph("Adherencia").setFontSize(8).setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(String.format("%.1f%%", adherence))
                        .setFontSize(14).setBold().setFontColor(color))
                .add(new Paragraph(label).setFontSize(8).setFontColor(color))
                .setBackgroundColor(LIGHT_GRAY).setPadding(6)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));

        doc.add(table);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addTopImpactMeals(Document doc, ReportData data) {
        if (data.getTopImpactMeals().isEmpty()) return;

        doc.add(sectionTitle("Comidas con Mayor Impacto Calórico"));

        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 17, 17, 17, 19}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableHeader(table, "Fecha", "Tipo", "Calorías", "Carbohidratos", "Proteínas");

        data.getTopImpactMeals().forEach(m -> {
            table.addCell(bodyCell(m.getConsumedAt().format(DATE_FMT)));
            table.addCell(bodyCell(formatMealType(m.getMealType().name())));
            table.addCell(bodyCell(m.getTotalCalories() + " kcal"));
            table.addCell(bodyCell(m.getTotalCarbohydrates() + " g"));
            table.addCell(bodyCell(m.getTotalProteins() + " g"));
        });

        doc.add(table);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addExerciseSummary(Document doc, ReportData data) {
        if (data.getExerciseLogs().isEmpty()) return;

        doc.add(sectionTitle("Actividad Física en el Período"));

        long totalSessions = data.getExerciseLogs().size();
        int  totalMinutes  = data.getExerciseLogs().stream()
                .mapToInt(ExerciseLog::getDurationMinutes).sum();
        double totalCalories = data.getExerciseLogs().stream()
                .mapToDouble(e -> e.getCaloriesBurned() != null ?
                        e.getCaloriesBurned().doubleValue() : 0)
                .sum();

        Table summary = new Table(UnitValue.createPercentArray(new float[]{33, 33, 33}))
                .setWidth(UnitValue.createPercentValue(100));

        addInfoCell(summary, "Sesiones totales", totalSessions + " sesiones");
        addInfoCell(summary, "Tiempo total", totalMinutes + " minutos");
        addInfoCell(summary, "Calorías quemadas", String.format("%.0f kcal", totalCalories));

        doc.add(summary);
        doc.add(new Paragraph("\n").setFontSize(4));

        Table detail = new Table(UnitValue.createPercentArray(new float[]{30, 25, 20, 25}))
                .setWidth(UnitValue.createPercentValue(100));

        addTableHeader(detail, "Fecha", "Ejercicio", "Duración", "Intensidad");

        data.getExerciseLogs().stream().limit(10).forEach(e -> {
            detail.addCell(bodyCell(e.getPerformedAt().format(DATE_FMT)));
            detail.addCell(bodyCell(formatExerciseType(e.getExerciseType().name())));
            detail.addCell(bodyCell(e.getDurationMinutes() + " min"));
            detail.addCell(bodyCell(formatIntensity(e.getIntensity().name())));
        });

        doc.add(detail);
        doc.add(new Paragraph("\n").setFontSize(4));
    }

    private void addMenstrualCycle(Document doc, ReportData data) {
        if (data.getLatestMenstrualCycle() == null) return;

        doc.add(sectionTitle("Ciclo Menstrual"));

        MenstrualCycle cycle = data.getLatestMenstrualCycle();
        var currentPhase = cycle.calculateCurrentPhase(
                LocalDate.now(), data.getAverageCycleLength(), data.getAveragePeriodLength());
        Table table = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}))
                .setWidth(UnitValue.createPercentValue(100));

        addInfoCell(table, "Inicio del ciclo",
                cycle.getStartDate().format(DATE_ONLY));
        addInfoCell(table, "Fase actual",
                cycleGuidanceService.resolveLabel(currentPhase));
        addInfoCell(table, "Próximo ciclo",
                cycle.predictNextCycleStart(data.getAverageCycleLength()).format(DATE_ONLY));
        addInfoCell(table, "Duración promedio",
                (data.getAverageCycleLength() != null ? data.getAverageCycleLength() : 28) + " días");

        doc.add(table);

        doc.add(new Paragraph(cycleGuidanceService.resolveGuidance(currentPhase))
                .setFontSize(9).setFontColor(ColorConstants.DARK_GRAY)
                .setBackgroundColor(LIGHT_GRAY)
                .setPadding(8).setMarginTop(6).setMarginBottom(8));
    }

    private String formatMealType(String type) {
        return switch (type) {
            case "BREAKFAST" -> "Desayuno";
            case "LUNCH"     -> "Almuerzo";
            case "DINNER"    -> "Cena";
            case "SNACK"     -> "Merienda";
            default          -> type;
        };
    }

    private String formatExerciseType(String type) {
        return switch (type) {
            case "WALKING"         -> "Caminata";
            case "RUNNING"         -> "Trote";
            case "CYCLING"         -> "Ciclismo";
            case "SWIMMING"        -> "Natación";
            case "WEIGHT_TRAINING" -> "Pesas";
            case "YOGA"            -> "Yoga";
            case "FOOTBALL"        -> "Fútbol";
            case "BASKETBALL"      -> "Baloncesto";
            case "DANCING"         -> "Baile";
            case "HIKING"          -> "Senderismo";
            default                -> "Otro";
        };
    }

    private String formatIntensity(String intensity) {
        return switch (intensity) {
            case "LOW"      -> "Baja";
            case "MODERATE" -> "Moderada";
            case "HIGH"     -> "Alta";
            default         -> intensity;
        };
    }
}