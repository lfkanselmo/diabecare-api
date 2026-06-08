package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.ExerciseIntensity;
import com.diabecare.domain.model.ExerciseLog;
import com.diabecare.domain.model.ExerciseType;
import com.diabecare.infrastructure.persistence.entity.ExerciseLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {ExerciseType.class, ExerciseIntensity.class})
public interface ExerciseLogPersistenceMapper {

    @Mapping(target = "id", source = "exerciseId")
    @Mapping(target = "exerciseType", expression = "java(log.getExerciseType().name())")
    @Mapping(target = "intensity", expression = "java(log.getIntensity().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ExerciseLogEntity toEntity(ExerciseLog log);

    @Mapping(target = "exerciseId", source = "id")
    @Mapping(target = "exerciseType", expression = "java(ExerciseType.valueOf(entity.getExerciseType()))")
    @Mapping(target = "intensity", expression = "java(ExerciseIntensity.valueOf(entity.getIntensity()))")
    ExerciseLog toDomain(ExerciseLogEntity entity);
}