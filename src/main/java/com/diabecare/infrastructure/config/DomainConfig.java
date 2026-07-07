package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.service.AuditService;
import com.diabecare.domain.service.CycleLabelService;
import com.diabecare.domain.service.CycleStatisticsService;
import com.diabecare.domain.service.ExerciseLabelService;
import com.diabecare.domain.service.GlucoseExportService;
import com.diabecare.domain.service.MedicalCalculatorService;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.diabecare.domain.service.MetadataLabelService;
import com.diabecare.domain.service.PatternDetectorService;
import com.diabecare.domain.service.WeeklySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DomainConfig {

    private final SystemConfigPort    systemConfig;
    private final MessageResolverPort messages;

    @Bean
    public MedicalCalculatorService medicalCalculatorService() {
        return new MedicalCalculatorService();
    }

    @Bean
    public GlucoseExportService glucoseExportService() {
        return new GlucoseExportService();
    }

    @Bean
    public AuditService auditService() {
        return new AuditService();
    }

    @Bean
    public PatternDetectorService patternDetectorService(MedicalCalculatorService calculator) {
        return new PatternDetectorService(calculator, systemConfig, messages);
    }

    @Bean
    public WeeklySummaryService weeklySummaryService(MedicalCalculatorService calculator) {
        return new WeeklySummaryService(calculator, messages);
    }

    @Bean
    public MenstrualCycleGuidanceService menstrualCycleGuidanceService() {
        return new MenstrualCycleGuidanceService(messages);
    }

    @Bean
    public CycleStatisticsService cycleStatisticsService() {
        return new CycleStatisticsService();
    }

    @Bean
    public CycleLabelService cycleLabelService() {
        return new CycleLabelService(messages);
    }

    @Bean
    public ExerciseLabelService exerciseLabelService() {
        return new ExerciseLabelService(messages);
    }

    @Bean
    public MetadataLabelService metadataLabelService() {
        return new MetadataLabelService(messages);
    }
}