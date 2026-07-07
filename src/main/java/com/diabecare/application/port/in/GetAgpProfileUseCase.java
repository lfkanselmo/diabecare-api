package com.diabecare.application.port.in;

import com.diabecare.domain.model.AgpHourlyBucket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GetAgpProfileUseCase {
    List<AgpHourlyBucket> execute(UUID patientId, LocalDateTime from, LocalDateTime to);
}
