package com.diabecare.application.port.out;

import com.diabecare.domain.model.MobilePlatform;
import com.diabecare.domain.model.MobilePushToken;

import java.util.List;
import java.util.UUID;

public interface MobilePushTokenPort {
    boolean existsByPatientIdAndDeviceToken(UUID patientId, String deviceToken);
    void save(UUID patientId, String deviceToken, MobilePlatform platform);
    void deleteByDeviceToken(String deviceToken);
    List<MobilePushToken> findAllByPatientId(UUID patientId);
}
