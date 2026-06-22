package com.diabecare.infrastructure.persistence.mapper;

import com.diabecare.domain.model.PushSubscription;
import com.diabecare.infrastructure.persistence.entity.PushSubscriptionEntity;
import org.mapstruct.Mapper;

@Mapper
public interface PushSubscriptionPersistenceMapper {
    PushSubscription toDomain(PushSubscriptionEntity entity);
}