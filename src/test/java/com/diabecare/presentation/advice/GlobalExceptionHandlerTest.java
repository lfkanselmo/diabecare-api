package com.diabecare.presentation.advice;

import com.diabecare.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/patients/123");
    }

    @Nested
    @DisplayName("handlers de dominio")
    class DomainHandlers {

        @Test
        @DisplayName("handlePatientNotFound retorna 404 con código PATIENT_NOT_FOUND")
        void handlePatientNotFoundReturns404() {
            var ex = new PatientNotFoundException("id-123");

            ResponseEntity<ApiError> response = handler.handlePatientNotFound(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().code()).isEqualTo("PATIENT_NOT_FOUND");
            assertThat(response.getBody().path()).isEqualTo("/api/v1/patients/123");
        }

        @Test
        @DisplayName("handleGlucoseNotFound retorna 404 con código GLUCOSE_READING_NOT_FOUND")
        void handleGlucoseNotFoundReturns404() {
            var ex = new GlucoseReadingNotFoundException("id-456");

            ResponseEntity<ApiError> response = handler.handleGlucoseNotFound(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody().code()).isEqualTo("GLUCOSE_READING_NOT_FOUND");
        }

        @Test
        @DisplayName("handleOpenCycleConflict retorna 409 con código OPEN_CYCLE_CONFLICT")
        void handleOpenCycleConflictReturns409() {
            var ex = new OpenCycleConflictException(LocalDate.of(2026, 6, 1));

            ResponseEntity<ApiError> response = handler.handleOpenCycleConflict(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody().code()).isEqualTo("OPEN_CYCLE_CONFLICT");
        }

        @Test
        @DisplayName("handleDomainValidation retorna 400 con código DOMAIN_VALIDATION_ERROR para InvalidPatientDataException")
        void handleDomainValidationReturns400ForInvalidPatientData() {
            var ex = new InvalidPatientDataException("nombre inválido");

            ResponseEntity<ApiError> response = handler.handleDomainValidation(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().code()).isEqualTo("DOMAIN_VALIDATION_ERROR");
            assertThat(response.getBody().message()).isEqualTo("nombre inválido");
        }

        @Test
        @DisplayName("handleDomainValidation también maneja InvalidGlucoseReadingException")
        void handleDomainValidationAlsoHandlesInvalidGlucoseReading() {
            var ex = new InvalidGlucoseReadingException("valor fuera de rango");

            ResponseEntity<ApiError> response = handler.handleDomainValidation(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("handleRateLimit retorna 429 con código RATE_LIMIT_EXCEEDED")
        void handleRateLimitReturns429() {
            var ex = new RateLimitExceededException("límite excedido");

            ResponseEntity<ApiError> response = handler.handleRateLimit(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
            assertThat(response.getBody().code()).isEqualTo("RATE_LIMIT_EXCEEDED");
        }

        @Test
        @DisplayName("handleInvalidRefreshToken retorna 401 con código INVALID_REFRESH_TOKEN")
        void handleInvalidRefreshTokenReturns401() {
            var ex = new InvalidRefreshTokenException();

            ResponseEntity<ApiError> response = handler.handleInvalidRefreshToken(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().code()).isEqualTo("INVALID_REFRESH_TOKEN");
        }

        @Test
        @DisplayName("handleUnauthorizedResourceAccess retorna 403 con código UNAUTHORIZED_RESOURCE_ACCESS")
        void handleUnauthorizedResourceAccessReturns403() {
            var ex = new UnauthorizedResourceAccessException("sin permiso");

            ResponseEntity<ApiError> response = handler.handleUnauthorizedResourceAccess(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().code()).isEqualTo("UNAUTHORIZED_RESOURCE_ACCESS");
        }
    }

    @Nested
    @DisplayName("handlers de seguridad")
    class SecurityHandlers {

        @Test
        @DisplayName("handleDisabled retorna 403 con código ACCOUNT_SUSPENDED y mensaje fijo")
        void handleDisabledReturns403WithFixedMessage() {
            var ex = new DisabledException("cuenta deshabilitada");

            ResponseEntity<ApiError> response = handler.handleDisabled(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody().code()).isEqualTo("ACCOUNT_SUSPENDED");
            assertThat(response.getBody().message()).contains("suspendida");
        }

        @Test
        @DisplayName("handleBadCredentials retorna 401 con código INVALID_CREDENTIALS y mensaje genérico")
        void handleBadCredentialsReturns401WithGenericMessage() {
            var ex = new BadCredentialsException("contraseña incorrecta en BD");

            ResponseEntity<ApiError> response = handler.handleBadCredentials(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody().code()).isEqualTo("INVALID_CREDENTIALS");
            // El mensaje debe ser genérico, sin filtrar detalles internos de la excepción real
            assertThat(response.getBody().message()).doesNotContain("BD");
        }
    }

    @Nested
    @DisplayName("handleValidation")
    class HandleValidation {

        @Test
        @DisplayName("retorna 400 con código VALIDATION_ERROR y la lista de errores de campo")
        void returns400WithValidationErrorCodeAndFieldErrorsList() {
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError = new FieldError("command", "email", "no puede estar vacío");

            when(ex.getBindingResult()).thenReturn(bindingResult);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

            ResponseEntity<ApiError> response = handler.handleValidation(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
            assertThat(response.getBody().fieldErrors()).hasSize(1);
            assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("email");
            assertThat(response.getBody().fieldErrors().get(0).message()).isEqualTo("no puede estar vacío");
        }
    }

    @Nested
    @DisplayName("handleGeneric")
    class HandleGeneric {

        @Test
        @DisplayName("retorna 500 con código INTERNAL_ERROR y mensaje genérico, sin exponer detalles internos")
        void returns500WithGenericMessageWithoutExposingInternalDetails() {
            var ex = new RuntimeException("NullPointerException en línea 42 de SecretService.java");

            ResponseEntity<ApiError> response = handler.handleGeneric(ex, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody().code()).isEqualTo("INTERNAL_ERROR");
            assertThat(response.getBody().message()).doesNotContain("SecretService");
        }
    }
}