package com.diabecare.presentation.mapper;

import com.diabecare.domain.model.VitalSign;
import com.diabecare.presentation.dto.response.VitalSignResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface VitalSignPresentationMapper {

    @Mapping(target = "bmi", expression = "java(vitalSign.calculateBmi())")
    @Mapping(target = "bmiCategory", expression = "java(vitalSign.getBmiCategory() != null ? vitalSign.getBmiCategory().name() : null)")
    VitalSignResponse toResponse(VitalSign vitalSign);
}