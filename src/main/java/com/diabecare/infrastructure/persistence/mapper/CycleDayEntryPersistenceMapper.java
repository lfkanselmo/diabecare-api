package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.CycleDayEntry;
import com.diabecare.domain.model.CycleSymptomEntry;
import com.diabecare.domain.model.FlowIntensity;
import com.diabecare.domain.model.SymptomSeverity;
import com.diabecare.infrastructure.persistence.entity.CycleDayEntryEntity;
import com.diabecare.infrastructure.persistence.entity.CycleDaySymptomEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = {FlowIntensity.class, SymptomSeverity.class})
public interface CycleDayEntryPersistenceMapper {

    @Mapping(target = "id", source = "dayEntryId")
    @Mapping(target = "flowIntensity", expression = "java(entry.getFlowIntensity().name())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "symptoms", ignore = true)
    CycleDayEntryEntity toEntity(CycleDayEntry entry);

    @Mapping(target = "dayEntryId", source = "id")
    @Mapping(target = "flowIntensity", expression = "java(FlowIntensity.valueOf(entity.getFlowIntensity()))")
    CycleDayEntry toDomain(CycleDayEntryEntity entity);

    @Mapping(target = "symptom", expression = "java(com.diabecare.domain.model.CycleSymptom.valueOf(entity.getSymptomCode()))")
    @Mapping(target = "severity", expression = "java(SymptomSeverity.valueOf(entity.getSeverity()))")
    CycleSymptomEntry toSymptomDomain(CycleDaySymptomEntity entity);
}