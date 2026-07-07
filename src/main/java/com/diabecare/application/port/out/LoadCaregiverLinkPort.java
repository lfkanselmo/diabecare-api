package com.diabecare.application.port.out;

import com.diabecare.domain.model.CaregiverLink;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadCaregiverLinkPort {

    Optional<CaregiverLink> findById(UUID linkId);

    boolean existsActive(UUID patientId, UUID caregiverUserId);

    List<CaregiverLink> findActiveByPatientId(UUID patientId);

    List<CaregiverLink> findActiveByCaregiverUserId(UUID caregiverUserId);
}
