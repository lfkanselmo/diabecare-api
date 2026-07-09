package com.diabecare.infrastructure.food;

import com.diabecare.application.port.out.FoodLookupPort;
import com.diabecare.domain.model.ExternalFoodInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * Consulta la API pública y gratuita de Open Food Facts por código de barras,
 * usando java.net.http (mismo enfoque que ResendEmailAdapter — sin agregar un
 * cliente HTTP reactivo solo para esto). A diferencia del correo, un fallo aquí
 * no tiene un camino "silencioso" razonable: si la API no responde, el usuario
 * simplemente no obtiene datos y puede seguir registrando la comida a mano.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenFoodFactsAdapter implements FoodLookupPort {

    private static final String FIELDS = "product_name,brands,nutriments";
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "openFoodFactsLookup", key = "#barcode")
    public Optional<ExternalFoodInfo> findByBarcode(String barcode) {
        URI uri = URI.create("https://world.openfoodfacts.org/api/v2/product/" + barcode
                + ".json?fields=" + FIELDS);

        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Open Food Facts respondió {} para el código {}", response.statusCode(), barcode);
                return Optional.empty();
            }

            return parseResponse(barcode, response.body());
        } catch (IOException e) {
            log.warn("Error de red consultando Open Food Facts para el código {}: {}", barcode, e.getMessage());
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Consulta a Open Food Facts interrumpida para el código {}", barcode);
            return Optional.empty();
        }
    }

    // Sin "private": expuesto a tests en el mismo paquete para poder verificar el
    // parseo de forma determinística, sin depender de una llamada de red real.
    Optional<ExternalFoodInfo> parseResponse(String barcode, String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);

        if (root.path("status").asInt(0) == 0) {
            return Optional.empty();
        }

        JsonNode product = root.path("product");
        JsonNode nutriments = product.path("nutriments");

        return Optional.of(ExternalFoodInfo.builder()
                .barcode(barcode)
                .name(textOrNull(product, "product_name"))
                .brand(textOrNull(product, "brands"))
                .caloriesPer100g(decimalOrNull(nutriments, "energy-kcal_100g"))
                .carbsPer100g(decimalOrNull(nutriments, "carbohydrates_100g"))
                .proteinsPer100g(decimalOrNull(nutriments, "proteins_100g"))
                .fatsPer100g(decimalOrNull(nutriments, "fat_100g"))
                .build());
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private BigDecimal decimalOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : BigDecimal.valueOf(value.asDouble());
    }
}
