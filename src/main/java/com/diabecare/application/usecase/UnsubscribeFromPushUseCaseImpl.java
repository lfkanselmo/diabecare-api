package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UnsubscribeFromPushUseCase;
import com.diabecare.application.port.out.PushSubscriptionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UnsubscribeFromPushUseCaseImpl implements UnsubscribeFromPushUseCase {

    private final PushSubscriptionPort subscriptionPort;

    @Override
    public void execute(Command command) {
        subscriptionPort.deleteByEndpoint(command.endpoint());
    }
}
