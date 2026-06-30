package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.SystemConfig;
import com.diabecare.infrastructure.persistence.entity.SystemConfigEntity;
import com.diabecare.infrastructure.persistence.repository.SystemConfigJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SystemConfigAdapter")
class SystemConfigAdapterTest {

    @Mock
    private SystemConfigJpaRepository repository;

    private SystemConfigAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SystemConfigAdapter(repository);
    }

    @Nested
    @DisplayName("getInt / getDecimal / getString")
    class GetTypedValues {

        @Test
        @DisplayName("lanza IllegalArgumentException cuando la clave no está en caché (sin reload previo)")
        void throwsWhenKeyNotCachedWithoutPriorReload() {
            assertThatThrownBy(() -> adapter.getInt("alert.streak_days"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("alert.streak_days");
        }

        @Test
        @DisplayName("parsea correctamente un valor entero tras recargar el caché")
        void parsesIntegerValueCorrectlyAfterReload() {
            when(repository.findAll()).thenReturn(List.of(entityWith("alert.streak_days", "7")));
            adapter.reload();

            int result = adapter.getInt("alert.streak_days");

            assertThat(result).isEqualTo(7);
        }

        @Test
        @DisplayName("parsea correctamente un valor decimal tras recargar el caché")
        void parsesDecimalValueCorrectlyAfterReload() {
            when(repository.findAll()).thenReturn(List.of(entityWith("alert.good_tir_threshold", "70.5")));
            adapter.reload();

            double result = adapter.getDecimal("alert.good_tir_threshold");

            assertThat(result).isEqualTo(70.5);
        }

        @Test
        @DisplayName("retorna el valor de texto sin transformación tras recargar el caché")
        void returnsStringValueWithoutTransformationAfterReload() {
            when(repository.findAll()).thenReturn(List.of(entityWith("app.support_email", "soporte@diabecare.com")));
            adapter.reload();

            String result = adapter.getString("app.support_email");

            assertThat(result).isEqualTo("soporte@diabecare.com");
        }
    }

    @Nested
    @DisplayName("reload")
    class Reload {

        @Test
        @DisplayName("reemplaza completamente el caché anterior, descartando claves que ya no existen")
        void completelyReplacesPreviousCacheDiscardingRemovedKeys() {
            when(repository.findAll()).thenReturn(List.of(entityWith("key.a", "1")));
            adapter.reload();
            assertThat(adapter.getInt("key.a")).isEqualTo(1);

            when(repository.findAll()).thenReturn(List.of(entityWith("key.b", "2")));
            adapter.reload();

            assertThat(adapter.getInt("key.b")).isEqualTo(2);
            assertThatThrownBy(() -> adapter.getInt("key.a"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("retorna todas las configuraciones convertidas a dominio")
        void returnsAllConfigsConvertedToDomain() {
            SystemConfigEntity entity = SystemConfigEntity.builder()
                    .key("rate_limit.glucose_per_hour")
                    .value("20")
                    .dataType("INTEGER")
                    .category("RATE_LIMIT")
                    .description("Límite de lecturas por hora")
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(repository.findAll()).thenReturn(List.of(entity));

            List<SystemConfig> result = adapter.findAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getKey()).isEqualTo("rate_limit.glucose_per_hour");
            assertThat(result.get(0).getDataType()).isEqualTo(SystemConfig.DataType.INTEGER);
            assertThat(result.get(0).getCategory()).isEqualTo(SystemConfig.Category.RATE_LIMIT);
        }
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private SystemConfigEntity entityWith(String key, String value) {
        return SystemConfigEntity.builder()
                .key(key)
                .value(value)
                .dataType("INTEGER")
                .category("ALERTS")
                .updatedAt(LocalDateTime.now())
                .build();
    }
}