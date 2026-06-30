package com.diabecare.application.usecase;

import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.model.SystemConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetSystemConfigUseCaseImpl")
class GetSystemConfigUseCaseTest {

    @Mock
    private SystemConfigPort systemConfigPort;

    @InjectMocks
    private GetSystemConfigUseCaseImpl useCase;

    @Nested
    @DisplayName("getAll")
    class GetAll {

        @Test
        @DisplayName("retorna todas las configuraciones del sistema")
        void returnsAllSystemConfigurations() {
            SystemConfig config = SystemConfig.builder()
                    .key("rate_limit.glucose_per_hour")
                    .value("20")
                    .dataType(SystemConfig.DataType.INTEGER)
                    .category(SystemConfig.Category.RATE_LIMIT)
                    .build();

            when(systemConfigPort.findAll()).thenReturn(List.of(config));

            List<SystemConfig> result = useCase.getAll();

            assertThat(result).containsExactly(config);
        }
    }

    @Nested
    @DisplayName("reload")
    class Reload {

        @Test
        @DisplayName("delega la recarga de configuración al puerto")
        void delegatesReloadToPort() {
            useCase.reload();

            verify(systemConfigPort).reload();
        }
    }
}