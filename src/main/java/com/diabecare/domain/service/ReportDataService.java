package com.diabecare.domain.service;

import com.diabecare.domain.model.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ReportDataService {

    private Patient patient;
    private List<GlucoseReading> glucoseReadings;
    private List<MealEntry> mealEntries;
    private List<VitalSign> vitalSigns;
    private List<Medication> medications;
    private List<ExerciseLog> exerciseLogs;
    private MenstrualCycle latestMenstrualCycle;

    // Métricas glucémicas básicas
    private BigDecimal estimatedHba1c;
    private BigDecimal timeInRangePercent;
    private BigDecimal averageGlucose;
    private BigDecimal coefficientOfVariation;

    // TIR detallado
    private Map<String, BigDecimal> tirDetailed;

    // Promedios por tipo de lectura
    private Map<String, BigDecimal> averageByReadingType;

    // Episodios de hipoglucemia
    private List<GlucoseReading> hypoglycemiaEvents;

    // Adherencia
    private double adherencePercent;

    // Correlación comidas con mayor impacto
    private List<MealEntry> topImpactMeals;
}