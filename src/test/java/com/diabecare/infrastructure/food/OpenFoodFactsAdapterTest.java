package com.diabecare.infrastructure.food;

import com.diabecare.domain.model.ExternalFoodInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenFoodFactsAdapter")
class OpenFoodFactsAdapterTest {

    private OpenFoodFactsAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new OpenFoodFactsAdapter(new ObjectMapper());
    }

    @Nested
    @DisplayName("parseResponse")
    class ParseResponse {

        @Test
        @DisplayName("mapea un producto encontrado con todos los nutrientes presentes")
        void mapsFoundProductWithAllNutrientsPresent() throws Exception {
            String body = """
                    {
                      "status": 1,
                      "status_verbose": "product found",
                      "product": {
                        "product_name": "Nutella",
                        "brands": "Ferrero",
                        "nutriments": {
                          "energy-kcal_100g": 539,
                          "carbohydrates_100g": 57.5,
                          "proteins_100g": 6.3,
                          "fat_100g": 30.9
                        }
                      }
                    }
                    """;

            Optional<ExternalFoodInfo> result = adapter.parseResponse("3017620422003", body);

            assertThat(result).isPresent();
            ExternalFoodInfo info = result.get();
            assertThat(info.getBarcode()).isEqualTo("3017620422003");
            assertThat(info.getName()).isEqualTo("Nutella");
            assertThat(info.getBrand()).isEqualTo("Ferrero");
            assertThat(info.getCaloriesPer100g()).isEqualByComparingTo(BigDecimal.valueOf(539));
            assertThat(info.getCarbsPer100g()).isEqualByComparingTo(BigDecimal.valueOf(57.5));
            assertThat(info.getProteinsPer100g()).isEqualByComparingTo(BigDecimal.valueOf(6.3));
            assertThat(info.getFatsPer100g()).isEqualByComparingTo(BigDecimal.valueOf(30.9));
        }

        @Test
        @DisplayName("retorna Optional vacío cuando status es 0 (producto no encontrado)")
        void returnsEmptyWhenStatusIsZero() throws Exception {
            String body = """
                    {
                      "status": 0,
                      "status_verbose": "product not found"
                    }
                    """;

            Optional<ExternalFoodInfo> result = adapter.parseResponse("000000000000", body);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("mapea nutrientes faltantes como null en vez de fallar")
        void mapsMissingNutrientsAsNullInsteadOfFailing() throws Exception {
            String body = """
                    {
                      "status": 1,
                      "product": {
                        "product_name": "Producto sin datos completos",
                        "nutriments": {
                          "carbohydrates_100g": 10
                        }
                      }
                    }
                    """;

            Optional<ExternalFoodInfo> result = adapter.parseResponse("111111111111", body);

            assertThat(result).isPresent();
            ExternalFoodInfo info = result.get();
            assertThat(info.getBrand()).isNull();
            assertThat(info.getCaloriesPer100g()).isNull();
            assertThat(info.getCarbsPer100g()).isEqualByComparingTo(BigDecimal.TEN);
            assertThat(info.getProteinsPer100g()).isNull();
            assertThat(info.getFatsPer100g()).isNull();
        }
    }
}
