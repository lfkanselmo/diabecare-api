package com.diabecare.infrastructure.config;

import com.diabecare.application.port.out.AlertConfigPort;
import com.diabecare.application.port.out.LoadGlucoseReadingPort;
import com.diabecare.application.port.out.LoadMealEntryPort;
import com.diabecare.application.port.out.LoadMenstrualCyclePort;
import com.diabecare.application.port.out.MessageResolverPort;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.service.AlertDetector;
import com.diabecare.domain.service.ClinicalPatternAlertDetector;
import com.diabecare.domain.service.CycleStatisticsService;
import com.diabecare.domain.service.GlucoseRangeAlertDetector;
import com.diabecare.domain.service.MedicalCalculatorService;
import com.diabecare.domain.service.MenstrualCycleAlertDetector;
import com.diabecare.domain.service.MenstrualCycleGuidanceService;
import com.diabecare.domain.service.NoMealAlertDetector;
import com.diabecare.domain.service.PatternDetectorService;
import com.diabecare.domain.service.PositiveStreakAlertDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Ensambla la lista ordenada de {@link AlertDetector} que {@code GetAlertsUseCaseImpl}
 * recorre. Agregar un nuevo tipo de alerta = agregar una clase + una línea en la lista
 * de abajo, sin tocar el use case (principio abierto/cerrado).
 *
 * Se ensambla manualmente (no vía @Component + escaneo) para evitar la ambigüedad de
 * Spring entre "un bean de tipo List&lt;AlertDetector&gt;" y "todos los beans de tipo
 * AlertDetector recolectados automáticamente" si ambos existieran a la vez.
 */
@Configuration
@RequiredArgsConstructor
public class AlertDetectorConfig {

    private final LoadGlucoseReadingPort loadGlucoseReadingPort;
    private final LoadMealEntryPort loadMealEntryPort;
    private final LoadMenstrualCyclePort loadMenstrualCyclePort;
    private final AlertConfigPort alertConfig;
    private final SystemConfigPort systemConfig;
    private final MessageResolverPort messages;

    @Bean
    public List<AlertDetector> alertDetectors(
            MedicalCalculatorService medicalCalculatorService,
            PatternDetectorService patternDetectorService,
            CycleStatisticsService cycleStatisticsService,
            MenstrualCycleGuidanceService cycleGuidanceService) {
        return List.of(
                new GlucoseRangeAlertDetector(
                        loadGlucoseReadingPort, alertConfig, medicalCalculatorService, systemConfig, messages),
                new NoMealAlertDetector(loadMealEntryPort, messages),
                new PositiveStreakAlertDetector(
                        loadGlucoseReadingPort, alertConfig, medicalCalculatorService, messages),
                new ClinicalPatternAlertDetector(
                        loadGlucoseReadingPort, alertConfig, systemConfig, patternDetectorService),
                new MenstrualCycleAlertDetector(
                        loadMenstrualCyclePort, cycleStatisticsService, messages, cycleGuidanceService, alertConfig)
        );
    }
}
