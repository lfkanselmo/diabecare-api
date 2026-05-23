package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.GlucoseReading;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.ReadingType;
import com.diabecare.infrastructure.persistence.entity.GlucoseReadingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {GlucoseUnit.class, ReadingType.class})
public interface GlucoseReadingPersistenceMapper {

    @Mapping(target = "id", source = "readingId")
    @Mapping(target = "unit", expression = "java(reading.getUnit().name())")
    @Mapping(target = "readingType", expression = "java(reading.getReadingType().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GlucoseReadingEntity toEntity(GlucoseReading reading);

    @Mapping(target = "readingId", source = "id")
    @Mapping(target = "unit", expression = "java(GlucoseUnit.valueOf(entity.getUnit()))")
    @Mapping(target = "readingType", expression = "java(ReadingType.valueOf(entity.getReadingType()))")
    GlucoseReading toDomain(GlucoseReadingEntity entity);
}