package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.Medication;
import com.diabecare.presentation.dto.response.MedicationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface MedicationPresentationMapper {

    @Mapping(target = "type", expression = "java(medication.getType().name())")
    @Mapping(target = "doseUnit", expression = "java(medication.getDoseUnit().name())")
    @Mapping(target = "frequency", expression = "java(medication.getFrequency().name())")
    MedicationResponse toResponse(Medication medication);
}