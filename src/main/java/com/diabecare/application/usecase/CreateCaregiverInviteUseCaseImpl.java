package com.diabecare.application.usecase;

import com.diabecare.application.port.in.CreateCaregiverInviteUseCase;
import com.diabecare.application.port.out.CaregiverInvitePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateCaregiverInviteUseCaseImpl implements CreateCaregiverInviteUseCase {

    private static final long VALIDITY_DAYS = 7;

    private final CaregiverInvitePort caregiverInvitePort;

    @Override
    public Result execute(UUID patientId) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(VALIDITY_DAYS);
        CaregiverInvitePort.IssuedInvite issued = caregiverInvitePort.issue(patientId, expiresAt);
        return new Result(issued.rawCode(), issued.expiresAt());
    }
}
