package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SubscribeToPushUseCase;
import com.diabecare.application.port.out.PushSubscriptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscribeToPushUseCaseImpl implements SubscribeToPushUseCase {

    private final PushSubscriptionPort subscriptionPort;

    @Override
    public void execute(Command command) {
        if (subscriptionPort.existsByPatientIdAndEndpoint(command.patientId(), command.endpoint())) {
            return;
        }
        subscriptionPort.save(command.patientId(), command.endpoint(), command.p256dh(), command.auth());
    }
}
