package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UnsubscribeFromPushUseCase;
import com.diabecare.application.port.out.PushSubscriptionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnsubscribeFromPushUseCaseImpl")
class UnsubscribeFromPushUseCaseImplTest {

    @Mock
    private PushSubscriptionPort subscriptionPort;

    private UnsubscribeFromPushUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UnsubscribeFromPushUseCaseImpl(subscriptionPort);
    }

    @Test
    @DisplayName("elimina la suscripción por su endpoint")
    void deletesSubscriptionByEndpoint() {
        useCase.execute(new UnsubscribeFromPushUseCase.Command("endpoint-1"));

        verify(subscriptionPort).deleteByEndpoint("endpoint-1");
    }
}
