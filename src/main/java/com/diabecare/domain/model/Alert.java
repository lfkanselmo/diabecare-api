package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Alert {

    public enum Severity { SUCCESS, INFO, WARNING, DANGER }
    public enum AlertType {
        GLUCOSE_OUT_OF_RANGE,
        GLUCOSE_AVERAGE_HIGH,
        NO_GLUCOSE_RECORDED,
        CALORIE_GOAL_EXCEEDED,
        POSITIVE_STREAK,
        HIGH_HBA1C_ESTIMATED,
        NO_MEAL_RECORDED,
        GLUCOSE_PATTERN_DETECTED
    }

    private AlertType type;
    private Severity severity;
    private String title;
    private String message;
}