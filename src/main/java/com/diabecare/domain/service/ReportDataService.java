package com.diabecare.domain.service;

import com.diabecare.domain.model.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class ReportDataService {

    private Patient patient;
    private List<GlucoseReading> glucoseReadings;
    private List<MealEntry> mealEntries;
    private List<VitalSign> vitalSigns;
    private List<Medication> medications;
    private BigDecimal estimatedHba1c;
    private BigDecimal timeInRangePercent;
    private BigDecimal averageGlucose;
    private BigDecimal coefficientOfVariation;
}