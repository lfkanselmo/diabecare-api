package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ReportData {

    private Patient patient;
    private List<GlucoseReading> glucoseReadings;
    private List<MealEntry> mealEntries;
    private List<VitalSign> vitalSigns;
    private List<Medication> medications;
    private List<ExerciseLog> exerciseLogs;
    private MenstrualCycle latestMenstrualCycle;
    private Integer averageCycleLength;
    private Integer averagePeriodLength;

    private BigDecimal estimatedHba1c;
    private BigDecimal timeInRangePercent;
    private BigDecimal averageGlucose;
    private BigDecimal coefficientOfVariation;

    private Map<String, BigDecimal> tirDetailed;

    private Map<String, BigDecimal> averageByReadingType;

    private List<GlucoseReading> hypoglycemiaEvents;

    private double adherencePercent;

    private List<MealEntry> topImpactMeals;
}