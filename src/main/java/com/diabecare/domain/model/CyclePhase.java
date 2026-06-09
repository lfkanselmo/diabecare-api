package com.diabecare.domain.model;

public enum CyclePhase {
    MENSTRUATION,   // Días 1-5: caída de hormonas, glucosa variable
    FOLLICULAR,     // Días 6-13: estrógeno sube, mayor sensibilidad a insulina
    OVULATION,      // Día 14: pico de estrógeno, glucosa puede bajar
    LUTEAL_EARLY,   // Días 15-21: progesterona sube, resistencia a insulina aumenta
    LUTEAL_LATE     // Días 22-28: máxima resistencia, mayor riesgo hiperglucemia
}