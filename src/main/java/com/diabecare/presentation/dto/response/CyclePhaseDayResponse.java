package com.diabecare.presentation.dto.response;

import java.time.LocalDate;

public record CyclePhaseDayResponse(
        LocalDate date,
        String phase
) {}