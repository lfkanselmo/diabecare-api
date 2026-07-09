package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.MobilePlatform;
import com.diabecare.domain.model.MobilePushToken;
import com.diabecare.infrastructure.persistence.entity.MobilePushTokenEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(imports = MobilePlatform.class)
public interface MobilePushTokenPersistenceMapper {

    @Mapping(target = "platform", expression = "java(MobilePlatform.valueOf(entity.getPlatform()))")
    MobilePushToken toDomain(MobilePushTokenEntity entity);
}
