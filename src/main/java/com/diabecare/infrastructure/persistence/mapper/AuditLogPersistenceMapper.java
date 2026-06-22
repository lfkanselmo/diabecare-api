package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.AuditLog;
import com.diabecare.infrastructure.persistence.entity.AuditLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = AuditLog.Action.class)
public interface AuditLogPersistenceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "action", expression = "java(auditLog.getAction().name())")
    @Mapping(target = "performedAt", ignore = true)
    AuditLogEntity toEntity(AuditLog auditLog);

    @Mapping(target = "action", expression = "java(AuditLog.Action.valueOf(entity.getAction()))")
    AuditLog toDomain(AuditLogEntity entity);
}