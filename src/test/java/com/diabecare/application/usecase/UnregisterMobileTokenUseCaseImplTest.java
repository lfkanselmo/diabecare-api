package com.diabecare.application.usecase;

import com.diabecare.application.port.in.UnregisterMobileTokenUseCase;
import com.diabecare.application.port.out.MobilePushTokenPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UnregisterMobileTokenUseCaseImpl")
class UnregisterMobileTokenUseCaseImplTest {

    @Mock
    private MobilePushTokenPort mobilePushTokenPort;

    private UnregisterMobileTokenUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UnregisterMobileTokenUseCaseImpl(mobilePushTokenPort);
    }

    @Test
    @DisplayName("elimina el token por su valor")
    void deletesTokenByValue() {
        useCase.execute(new UnregisterMobileTokenUseCase.Command("token-1"));

        verify(mobilePushTokenPort).deleteByDeviceToken("token-1");
    }
}
