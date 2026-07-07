package com.diabecare.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class AgpHourlyBucket {

    private int hour;
    private BigDecimal p10;
    private BigDecimal p25;
    private BigDecimal median;
    private BigDecimal p75;
    private BigDecimal p90;
    private int readingCount;
}
