package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.CyclePhase;
import com.diabecare.domain.model.MenstrualCycle;
import com.diabecare.infrastructure.persistence.entity.MenstrualCycleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {CyclePhase.class})
public interface MenstrualCyclePersistenceMapper {

    @Mapping(target = "id", source = "cycleId")
    @Mapping(target = "phase", expression = "java(cycle.getPhase().name())")
    @Mapping(target = "createdAt", ignore = true)
    MenstrualCycleEntity toEntity(MenstrualCycle cycle);

    @Mapping(target = "cycleId", source = "id")
    @Mapping(target = "phase", expression = "java(CyclePhase.valueOf(entity.getPhase()))")
    MenstrualCycle toDomain(MenstrualCycleEntity entity);
}