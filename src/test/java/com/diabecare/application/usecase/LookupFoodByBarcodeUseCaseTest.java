package com.diabecare.application.usecase;

import com.diabecare.application.port.out.FoodLookupPort;
import com.diabecare.domain.model.ExternalFoodInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LookupFoodByBarcodeUseCaseImpl")
class LookupFoodByBarcodeUseCaseTest {

    @Mock
    private FoodLookupPort foodLookupPort;

    @InjectMocks
    private LookupFoodByBarcodeUseCaseImpl useCase;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("delega al puerto con el código de barras recortado")
        void delegatesToPortWithTrimmedBarcode() {
            ExternalFoodInfo info = ExternalFoodInfo.builder().barcode("123456").name("Producto").build();
            when(foodLookupPort.findByBarcode("123456")).thenReturn(Optional.of(info));

            Optional<ExternalFoodInfo> result = useCase.execute("  123456  ");

            assertThat(result).contains(info);
            verify(foodLookupPort).findByBarcode("123456");
        }

        @Test
        @DisplayName("retorna Optional vacío sin consultar el puerto cuando el código está en blanco")
        void returnsEmptyWithoutQueryingPortWhenBarcodeIsBlank() {
            Optional<ExternalFoodInfo> result = useCase.execute("   ");

            assertThat(result).isEmpty();
            verifyNoInteractions(foodLookupPort);
        }

        @Test
        @DisplayName("retorna Optional vacío cuando el puerto no encuentra el producto")
        void returnsEmptyWhenPortDoesNotFindProduct() {
            when(foodLookupPort.findByBarcode("000000")).thenReturn(Optional.empty());

            Optional<ExternalFoodInfo> result = useCase.execute("000000");

            assertThat(result).isEmpty();
        }
    }
}
