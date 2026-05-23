package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.*;
import com.diabecare.infrastructure.persistence.entity.PatientEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {DiabetesType.class, ActivityLevel.class, GlucoseUnit.class})
public interface PatientPersistenceMapper {

    @Mapping(target = "id", source = "patientId")
    @Mapping(target = "diabetesType", expression = "java(patient.getDiabetesType().name())")
    @Mapping(target = "activityLevel", expression = "java(patient.getActivityLevel().name())")
    @Mapping(target = "preferredGlucoseUnit", expression = "java(patient.getPreferredGlucoseUnit().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PatientEntity toEntity(Patient patient);

    @Mapping(target = "patientId", source = "id")
    @Mapping(target = "diabetesType", expression = "java(DiabetesType.valueOf(entity.getDiabetesType()))")
    @Mapping(target = "activityLevel", expression = "java(ActivityLevel.valueOf(entity.getActivityLevel()))")
    @Mapping(target = "preferredGlucoseUnit", expression = "java(GlucoseUnit.valueOf(entity.getPreferredGlucoseUnit()))")
    Patient toDomain(PatientEntity entity);
}