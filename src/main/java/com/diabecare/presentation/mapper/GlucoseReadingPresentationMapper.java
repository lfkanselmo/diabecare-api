package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.presentation.dto.response.GlucoseReadingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface GlucoseReadingPresentationMapper {

    @Mapping(target = "unit", expression = "java(reading.getUnit().name())")
    @Mapping(target = "readingType", expression = "java(reading.getReadingType().name())")
    @Mapping(target = "status", expression = "java(reading.getStatus().name())")
    GlucoseReadingResponse toResponse(GlucoseReading reading);
}