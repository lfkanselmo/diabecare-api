package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.ActivityLevel;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.GlucoseUnit;
import com.diabecare.domain.model.Patient;
import com.diabecare.presentation.dto.response.PatientResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {DiabetesType.class, ActivityLevel.class, GlucoseUnit.class})
public interface PatientPresentationMapper {

    @Mapping(target = "age", expression = "java(patient.getAge())")
    @Mapping(target = "diabetesType", expression = "java(patient.getDiabetesType().name())")
    @Mapping(target = "activityLevel", expression = "java(patient.getActivityLevel().name())")
    @Mapping(target = "preferredGlucoseUnit", expression = "java(patient.getPreferredGlucoseUnit().name())")
    @Mapping(target = "biologicalSex", expression = "java(patient.getBiologicalSex() != null ? patient.getBiologicalSex().name() : \"NOT_SPECIFIED\")")
    PatientResponse toResponse(Patient patient);
}