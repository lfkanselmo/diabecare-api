package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadFoodPort;
import com.diabecare.domain.model.Food;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchFoodUseCaseImpl")
class SearchFoodUseCaseTest {

    @Mock
    private LoadFoodPort loadFoodPort;

    @InjectMocks
    private SearchFoodUseCaseImpl useCase;

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("retorna lista vacía cuando la búsqueda es nula")
        void returnsEmptyWhenQueryIsNull() {
            List<Food> result = useCase.search(null);

            assertThat(result).isEmpty();
            verifyNoInteractions(loadFoodPort);
        }

        @Test
        @DisplayName("retorna lista vacía cuando la búsqueda está en blanco")
        void returnsEmptyWhenQueryIsBlank() {
            List<Food> result = useCase.search("   ");

            assertThat(result).isEmpty();
            verifyNoInteractions(loadFoodPort);
        }

        @Test
        @DisplayName("retorna lista vacía cuando la búsqueda tiene menos de 2 caracteres")
        void returnsEmptyWhenQueryTooShort() {
            List<Food> result = useCase.search("a");

            assertThat(result).isEmpty();
            verifyNoInteractions(loadFoodPort);
        }

        @Test
        @DisplayName("realiza la búsqueda cuando la consulta tiene exactamente 2 caracteres")
        void searchesWhenQueryHasExactlyTwoChars() {
            when(loadFoodPort.searchByName("pa")).thenReturn(List.of(validFood()));

            List<Food> result = useCase.search("pa");

            assertThat(result).hasSize(1);
            verify(loadFoodPort).searchByName("pa");
        }

        @Test
        @DisplayName("recorta espacios al inicio y al final antes de buscar")
        void trimsWhitespaceBeforeSearching() {
            when(loadFoodPort.searchByName("pan")).thenReturn(List.of(validFood()));

            useCase.search("  pan  ");

            verify(loadFoodPort).searchByName("pan");
        }
    }

    @Nested
    @DisplayName("findByCategory")
    class FindByCategory {

        @Test
        @DisplayName("delega directamente al puerto sin ninguna validación adicional")
        void delegatesDirectlyToPort() {
            when(loadFoodPort.findByCategory("FRUITS")).thenReturn(List.of(validFood()));

            List<Food> result = useCase.findByCategory("FRUITS");

            assertThat(result).hasSize(1);
            verify(loadFoodPort).findByCategory("FRUITS");
        }
    }


    private Food validFood() {
        return Food.builder()
                .foodId(UUID.randomUUID())
                .name("Pan")
                .category("GRAINS")
                .caloriesPer100g(BigDecimal.valueOf(265))
                .carbsPer100g(BigDecimal.valueOf(49))
                .proteinsPer100g(BigDecimal.valueOf(9))
                .fatsPer100g(BigDecimal.valueOf(3.2))
                .build();
    }
}
