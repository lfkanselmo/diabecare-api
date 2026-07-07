package com.diabecare.application.usecase;

import com.diabecare.application.port.in.CreateCaregiverInviteUseCase;
import com.diabecare.application.port.out.CaregiverInvitePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCaregiverInviteUseCaseImpl")
class CreateCaregiverInviteUseCaseTest {

    @Mock
    private CaregiverInvitePort caregiverInvitePort;

    private CreateCaregiverInviteUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new CreateCaregiverInviteUseCaseImpl(caregiverInvitePort);
    }

    @Test
    @DisplayName("emite una invitación con vigencia de 7 días y retorna el código y su expiración")
    void issuesInviteWithSevenDayValidityAndReturnsCodeAndExpiry() {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        when(caregiverInvitePort.issue(eq(patientId), any()))
                .thenReturn(new CaregiverInvitePort.IssuedInvite("ABCD-1234", expiresAt));

        CreateCaregiverInviteUseCase.Result result = useCase.execute(patientId);

        assertThat(result.code()).isEqualTo("ABCD-1234");
        assertThat(result.expiresAt()).isEqualTo(expiresAt);

        ArgumentCaptor<LocalDateTime> expiryCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(caregiverInvitePort).issue(eq(patientId), expiryCaptor.capture());

        long daysUntilExpiry = Duration.between(LocalDateTime.now(), expiryCaptor.getValue()).toDays();
        assertThat(daysUntilExpiry).isBetween(6L, 7L);
    }
}
