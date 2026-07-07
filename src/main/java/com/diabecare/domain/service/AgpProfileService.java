package com.diabecare.domain.service;

import com.diabecare.domain.model.AgpHourlyBucket;
import com.diabecare.domain.model.GlucoseReading;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Construye el "perfil de día modal" (Ambulatory Glucose Profile): agrupa todas las
 * lecturas del rango por hora del día (sin importar la fecha) y calcula, por cada
 * una de las 24 horas, los percentiles 10/25/50/75/90 — la visualización estándar
 * de un reporte AGP clínico.
 */
public class AgpProfileService {

    public List<AgpHourlyBucket> buildHourlyProfile(List<GlucoseReading> readings) {
        Map<Integer, List<BigDecimal>> valuesByHour = new TreeMap<>();
        for (int hour = 0; hour < 24; hour++) {
            valuesByHour.put(hour, new ArrayList<>());
        }

        for (GlucoseReading reading : readings) {
            int hour = reading.getMeasuredAt().getHour();
            valuesByHour.get(hour).add(reading.getValueInMgDl());
        }

        List<AgpHourlyBucket> buckets = new ArrayList<>();
        for (Map.Entry<Integer, List<BigDecimal>> entry : valuesByHour.entrySet()) {
            List<BigDecimal> sorted = new ArrayList<>(entry.getValue());
            sorted.sort(Comparator.naturalOrder());

            buckets.add(AgpHourlyBucket.builder()
                    .hour(entry.getKey())
                    .p10(percentile(sorted, 10))
                    .p25(percentile(sorted, 25))
                    .median(percentile(sorted, 50))
                    .p75(percentile(sorted, 75))
                    .p90(percentile(sorted, 90))
                    .readingCount(sorted.size())
                    .build());
        }
        return buckets;
    }

    /**
     * Percentil por interpolación lineal entre los dos valores ordenados más cercanos
     * (método usado por la mayoría de hojas de cálculo y por AGP clínico estándar).
     */
    private BigDecimal percentile(List<BigDecimal> sortedValues, double percentileRank) {
        if (sortedValues.isEmpty()) return null;
        if (sortedValues.size() == 1) return sortedValues.get(0).setScale(1, RoundingMode.HALF_UP);

        double rank = (percentileRank / 100.0) * (sortedValues.size() - 1);
        int lowIndex = (int) Math.floor(rank);
        int highIndex = (int) Math.ceil(rank);

        if (lowIndex == highIndex) {
            return sortedValues.get(lowIndex).setScale(1, RoundingMode.HALF_UP);
        }

        BigDecimal low = sortedValues.get(lowIndex);
        BigDecimal high = sortedValues.get(highIndex);
        double fraction = rank - lowIndex;

        return low.add(high.subtract(low).multiply(BigDecimal.valueOf(fraction)))
                .setScale(1, RoundingMode.HALF_UP);
    }
}
