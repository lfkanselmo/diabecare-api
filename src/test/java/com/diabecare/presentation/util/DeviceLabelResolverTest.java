package com.diabecare.presentation.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DeviceLabelResolver")
class DeviceLabelResolverTest {

    @Nested
    @DisplayName("resolve")
    class Resolve {

        @Test
        @DisplayName("retorna 'Dispositivo desconocido' cuando el user-agent es nulo")
        void returnsUnknownDeviceWhenUserAgentIsNull() {
            assertThat(DeviceLabelResolver.resolve(null)).isEqualTo("Dispositivo desconocido");
        }

        @Test
        @DisplayName("retorna 'Dispositivo desconocido' cuando el user-agent está en blanco")
        void returnsUnknownDeviceWhenUserAgentIsBlank() {
            assertThat(DeviceLabelResolver.resolve("   ")).isEqualTo("Dispositivo desconocido");
        }

        @Test
        @DisplayName("identifica Chrome en Windows")
        void identifiesChromeOnWindows() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

            assertThat(DeviceLabelResolver.resolve(ua)).isEqualTo("Chrome en Windows");
        }

        @Test
        @DisplayName("identifica Safari en macOS, sin confundirlo con Chrome")
        void identifiesSafariOnMacOsWithoutConfusingWithChrome() {
            String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 " +
                    "(KHTML, like Gecko) Version/17.0 Safari/605.1.15";

            assertThat(DeviceLabelResolver.resolve(ua)).isEqualTo("Safari en macOS");
        }

        @Test
        @DisplayName("identifica Safari en iPhone")
        void identifiesSafariOnIphone() {
            String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 " +
                    "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";

            assertThat(DeviceLabelResolver.resolve(ua)).isEqualTo("Safari en iPhone");
        }

        @Test
        @DisplayName("identifica Chrome en Android")
        void identifiesChromeOnAndroid() {
            String ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36";

            assertThat(DeviceLabelResolver.resolve(ua)).isEqualTo("Chrome en Android");
        }

        @Test
        @DisplayName("identifica Firefox en Linux")
        void identifiesFirefoxOnLinux() {
            String ua = "Mozilla/5.0 (X11; Linux x86_64; rv:120.0) Gecko/20100101 Firefox/120.0";

            assertThat(DeviceLabelResolver.resolve(ua)).isEqualTo("Firefox en Linux");
        }

        @Test
        @DisplayName("identifica Edge en Windows, sin confundirlo con Chrome")
        void identifiesEdgeOnWindowsWithoutConfusingWithChrome() {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0";

            assertThat(DeviceLabelResolver.resolve(ua)).isEqualTo("Edge en Windows");
        }

        @Test
        @DisplayName("identifica iPad")
        void identifiesIpad() {
            String ua = "Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15 " +
                    "(KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1";

            assertThat(DeviceLabelResolver.resolve(ua)).isEqualTo("Safari en iPad");
        }

        @Test
        @DisplayName("retorna 'Navegador' cuando no se reconoce ningún navegador específico")
        void returnsGenericBrowserWhenNoneRecognized() {
            String ua = "SomeBot/1.0 (compatible; crawler)";

            assertThat(DeviceLabelResolver.resolve(ua)).isEqualTo("Navegador en dispositivo desconocido");
        }
    }
}