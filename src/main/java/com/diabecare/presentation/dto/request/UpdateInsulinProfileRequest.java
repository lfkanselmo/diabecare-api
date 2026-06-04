package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateInsulinProfileRequest(
        @NotNull @DecimalMin("1")
        BigDecimal sensitivityFactor,

        @NotNull @DecimalMin("1")
        BigDecimal carbRatio,

        @NotNull @DecimalMin("50")
        BigDecimal targetGlucose
) {}