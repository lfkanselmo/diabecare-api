package com.diabecare.presentation.mapper;

import com.diabecare.application.dto.DailySummaryRecord;
import com.diabecare.presentation.dto.response.DailySummaryResponse;
import org.mapstruct.Mapper;

@Mapper
public interface DailySummaryPresentationMapper {
    DailySummaryResponse toResponse(DailySummaryRecord record);
}