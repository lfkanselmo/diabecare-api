package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.*;
import com.diabecare.infrastructure.persistence.entity.MedicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {MedicationType.class, DoseUnit.class, MedicationFrequency.class})
public interface MedicationPersistenceMapper {

    @Mapping(target = "id", source = "medicationId")
    @Mapping(target = "type", expression = "java(medication.getType().name())")
    @Mapping(target = "doseUnit", expression = "java(medication.getDoseUnit().name())")
    @Mapping(target = "frequency", expression = "java(medication.getFrequency().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    MedicationEntity toEntity(Medication medication);

    @Mapping(target = "medicationId", source = "id")
    @Mapping(target = "type", expression = "java(MedicationType.valueOf(entity.getType()))")
    @Mapping(target = "doseUnit", expression = "java(DoseUnit.valueOf(entity.getDoseUnit()))")
    @Mapping(target = "frequency", expression = "java(MedicationFrequency.valueOf(entity.getFrequency()))")
    Medication toDomain(MedicationEntity entity);
}