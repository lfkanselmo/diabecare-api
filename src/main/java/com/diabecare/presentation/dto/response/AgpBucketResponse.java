package com.diabecare.presentation.dto.response;

import java.math.BigDecimal;

public record AgpBucketResponse(
        int hour,
        BigDecimal p10,
        BigDecimal p25,
        BigDecimal median,
        BigDecimal p75,
        BigDecimal p90,
        int readingCount
) {}
