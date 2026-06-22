package com.diabecare.application.port.out;

import com.diabecare.domain.model.PushSubscription;

import java.util.List;
import java.util.UUID;

public interface PushSubscriptionPort {
    boolean existsByPatientIdAndEndpoint(UUID patientId, String endpoint);
    void save(UUID patientId, String endpoint, String p256dh, String auth);
    void deleteByEndpoint(String endpoint);
    List<PushSubscription> findAllByPatientId(UUID patientId);
}