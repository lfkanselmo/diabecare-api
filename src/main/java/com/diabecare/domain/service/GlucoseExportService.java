package com.diabecare.domain.service;

import com.diabecare.domain.model.GlucoseReading;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class GlucoseExportService {

    private final ObjectMapper objectMapper;

    public GlucoseExportService() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String toCsv(List<GlucoseReading> readings) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // BOM para UTF-8 — Excel lo requiere para leer tildes correctamente
        sb.append("id,valor,unidad,tipo,estado,fecha,notas\n");

        readings.forEach(r -> sb.append(String.format("%s,%s,%s,%s,%s,%s,%s\n",
                r.getReadingId(),
                r.getValueInMgDl(),
                r.getUnit().name(),
                r.getReadingType().name(),
                r.getStatus().name(),
                r.getMeasuredAt(),
                r.getNotes() != null ? r.getNotes().replace(",", ";") : ""
        )));

        return sb.toString();
    }

    public String toJson(List<GlucoseReading> readings) {
        try {
            return objectMapper.writeValueAsString(readings);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando lecturas a JSON", e);
        }
    }
}