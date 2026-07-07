package com.diabecare.infrastructure.persistence;

import com.diabecare.infrastructure.persistence.entity.FoodEntity;
import com.diabecare.infrastructure.persistence.entity.SystemConfigEntity;
import com.diabecare.infrastructure.persistence.repository.FoodJpaRepository;
import com.diabecare.infrastructure.persistence.repository.SystemConfigJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integración real (requiere Docker) — la clase de prueba que los 900 tests
 * unitarios con mocks nunca ejercitan: arranca el contexto completo de Spring contra un
 * PostgreSQL real, aplica TODAS las migraciones de Flyway (V1..V18) y ejecuta las
 * consultas JPQL personalizadas contra el esquema real. Un typo en un @Query o una
 * migración rota fallarían aquí, no solo en producción.
 *
 * No corre con "mvn test" (Surefire) — el nombre termina en "IT", no en "Test", así que
 * Surefire lo ignora por convención. Corre con "mvn verify" (Failsafe), o directamente
 * desde el IDE. Requiere Docker corriendo localmente o en el runner de CI.
 */
@Testcontainers
@SpringBootTest
class DatabaseMigrationsIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void testOnlySecrets(DynamicPropertyRegistry registry) {
        // Nunca se usan para firmar/enviar nada real en este test — solo evitan que el
        // contexto falle al arrancar por placeholders sin resolver (${JWT_SECRET_KEY}, etc.)
        registry.add("JWT_SECRET_KEY", () -> "integration-test-secret-key-minimo-32-caracteres");
        registry.add("VAPID_PUBLIC_KEY", () -> "integration-test-vapid-public-key");
        registry.add("VAPID_PRIVATE_KEY", () -> "integration-test-vapid-private-key");
    }

    @Autowired
    private FoodJpaRepository foodJpaRepository;

    @Autowired
    private SystemConfigJpaRepository systemConfigJpaRepository;

    @Test
    @DisplayName("el contexto de Spring arranca y Flyway aplica todas las migraciones sin error")
    void contextLoadsWithAllMigrationsApplied() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    @DisplayName("el catálogo de alimentos sembrado por Flyway (635 registros) es buscable por nombre")
    void foodCatalogIsSeededAndSearchableCaseInsensitively() {
        List<FoodEntity> results = foodJpaRepository.searchByName("arepa", PageRequest.of(0, 10));

        assertThat(results).isNotEmpty();
        assertThat(results).allSatisfy(food ->
                assertThat(food.getName().toLowerCase()).contains("arepa"));
    }

    @Test
    @DisplayName("los parámetros de system_config (incluida la migración V18 de rate limit de auth) quedan sembrados")
    void systemConfigIsSeededIncludingLatestMigration() {
        List<SystemConfigEntity> all = systemConfigJpaRepository.findAll();

        assertThat(all.size()).isGreaterThanOrEqualTo(16);
        assertThat(all).extracting(SystemConfigEntity::getKey).contains(
                "pattern.fasting_threshold_mgdl",
                "rate_limit.login_per_hour",
                "rate_limit.register_per_hour");
    }
}
