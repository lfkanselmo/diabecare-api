package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SystemConfig {

    public enum DataType { INTEGER, DECIMAL, STRING, BOOLEAN }
    public enum Category { ALERTS, PATTERNS, RATE_LIMIT }

    private String      key;
    private String      value;
    private DataType    dataType;
    private Category    category;
    private String      description;
    private LocalDateTime updatedAt;
}