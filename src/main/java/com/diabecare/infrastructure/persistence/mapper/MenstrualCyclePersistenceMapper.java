package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.infrastructure.persistence.entity.MenstrualCycleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MenstrualCyclePersistenceMapper {

    @Mapping(target = "id", source = "cycleId")
    @Mapping(target = "createdAt", ignore = true)
    MenstrualCycleEntity toEntity(MenstrualCycle cycle);

    @Mapping(target = "cycleId", source = "id")
    MenstrualCycle toDomain(MenstrualCycleEntity entity);
}