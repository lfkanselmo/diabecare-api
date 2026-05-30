package com.diabecare.infrastructure.pdf;

import com.diabecare.domain.model.VitalSign;
import com.diabecare.domain.service.ReportDataService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MedicalReportPdfGenerator {

    private static final DeviceRgb PRIMARY     = new DeviceRgb(21, 101, 192);
    private static final DeviceRgb LIGHT_GRAY  = new DeviceRgb(245, 247, 250);
    private static final DeviceRgb SUCCESS     = new DeviceRgb(46, 125, 50);
    private static final DeviceRgb WARNING     = new DeviceRgb(245, 127, 23);
    private static final DeviceRgb DANGER      = new DeviceRgb(198, 40, 40);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(ReportDataService data, LocalDate from, LocalDate to) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);
        doc.setMargins(36, 36, 36, 36);

        addHeader(doc, data, from, to);
        addPatientInfo(doc, data);
        addGlucoseSummary(doc, data);
        addGlucoseHistory(doc, data);
        addVitalSigns(doc, data);
        addMedications(doc, data);
        addFooter(doc);

        doc.close();
        return baos.toByteArray();
    }

    private void addHeader(Document doc, ReportDataService data,
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

    private void addPatientInfo(Document doc, ReportDataService data) {
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

    private void addGlucoseSummary(Document doc, ReportDataService data) {
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

    private void addGlucoseHistory(Document doc, ReportDataService data) {
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

    private void addVitalSigns(Document doc, ReportDataService data) {
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

    private void addMedications(Document doc, ReportDataService data) {
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

    // ── Helpers ───────────────────────────────────────────────────────────────

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
}