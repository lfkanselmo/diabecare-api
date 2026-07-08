package com.diabecare.infrastructure.mail;

import com.diabecare.application.port.out.SendEmailPort;
import com.diabecare.infrastructure.config.DiabeCareProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * Envía correo transaccional vía la API REST de Resend usando java.net.http
 * (sin agregar un cliente HTTP reactivo solo para esto). Si no hay API key
 * configurada (desarrollo local sin RESEND_API_KEY), no intenta la llamada
 * y en su lugar registra el enlace en el log — así el flujo completo se
 * puede probar localmente sin una cuenta de Resend real.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResendEmailAdapter implements SendEmailPort {

    private static final URI RESEND_EMAILS_ENDPOINT = URI.create("https://api.resend.com/emails");

    private final DiabeCareProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void sendPasswordResetEmail(String toEmail, String rawResetToken) {
        String resetLink = properties.mail().frontendBaseUrl() + "/auth/reset-password?token=" + rawResetToken;
        String apiKey = properties.mail().resendApiKey();

        if (apiKey == null || apiKey.isBlank()) {
            log.warn("RESEND_API_KEY no configurada; correo de recuperación no enviado. " +
                    "Enlace (solo visible en logs de desarrollo): {}", resetLink);
            return;
        }

        Map<String, Object> body = Map.of(
                "from", properties.mail().fromAddress(),
                "to", List.of(toEmail),
                "subject", "Recupera tu contraseña de DiabeCare",
                "html", buildHtml(resetLink)
        );

        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(RESEND_EMAILS_ENDPOINT)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new EmailDeliveryException(
                        "Resend respondió " + response.statusCode() + ": " + response.body());
            }
        } catch (IOException e) {
            throw new EmailDeliveryException("Error de red al enviar el correo de recuperación", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EmailDeliveryException("Envío de correo interrumpido", e);
        }
    }

    private String buildHtml(String resetLink) {
        return """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2>Recupera tu contraseña</h2>
                  <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta de DiabeCare.</p>
                  <p><a href="%s" style="display:inline-block;padding:12px 24px;background:#5B4FCF;color:#fff;
                     text-decoration:none;border-radius:8px;">Restablecer contraseña</a></p>
                  <p>Este enlace vence en 1 hora. Si no solicitaste este cambio, puedes ignorar este correo.</p>
                </div>
                """.formatted(resetLink);
    }
}
