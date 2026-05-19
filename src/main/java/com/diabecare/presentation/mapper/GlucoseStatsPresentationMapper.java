package com.diabecare.presentation.mapper;

import com.diabecare.application.dto.GlucoseStatsRecord;
import com.diabecare.presentation.dto.response.GlucoseStatsResponse;
import org.mapstruct.Mapper;

@Mapper
public interface GlucoseStatsPresentationMapper {
    GlucoseStatsResponse toResponse(GlucoseStatsRecord record);
}