package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.VitalSign;
import com.diabecare.infrastructure.persistence.entity.VitalSignEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface VitalSignPersistenceMapper {

    @Mapping(target = "id", source = "vitalId")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    VitalSignEntity toEntity(VitalSign vitalSign);

    @Mapping(target = "vitalId", source = "id")
    VitalSign toDomain(VitalSignEntity entity);
}