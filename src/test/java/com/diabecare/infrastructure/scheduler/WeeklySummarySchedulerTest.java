package com.diabecare.infrastructure.scheduler;

import com.diabecare.application.port.in.SendWeeklySummaryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WeeklySummaryScheduler")
class WeeklySummarySchedulerTest {

    @Mock
    private SendWeeklySummaryUseCase sendWeeklySummaryUseCase;

    @InjectMocks
    private WeeklySummaryScheduler scheduler;

    @Test
    @DisplayName("delega el envío de resúmenes semanales al caso de uso")
    void delegatesWeeklySummarySendingToUseCase() {
        scheduler.sendWeeklySummaries();

        verify(sendWeeklySummaryUseCase).sendToAllPatients();
    }
}