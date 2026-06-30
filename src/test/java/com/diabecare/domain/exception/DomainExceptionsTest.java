package com.diabecare.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Excepciones de dominio")
class DomainExceptionsTest {

    @Nested
    @DisplayName("Excepciones con mensaje directo")
    class MessagePassthroughExceptions {

        @Test
        @DisplayName("InvalidGlucoseReadingException propaga el mensaje tal cual")
        void invalidGlucoseReadingExceptionPropagatesMessage() {
            var ex = new InvalidGlucoseReadingException("mensaje de prueba");
            assertThat(ex.getMessage()).isEqualTo("mensaje de prueba");
            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("InvalidMealEntryException propaga el mensaje tal cual")
        void invalidMealEntryExceptionPropagatesMessage() {
            var ex = new InvalidMealEntryException("mensaje de prueba");
            assertThat(ex.getMessage()).isEqualTo("mensaje de prueba");
            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("InvalidMedicationException propaga el mensaje tal cual")
        void invalidMedicationExceptionPropagatesMessage() {
            var ex = new InvalidMedicationException("mensaje de prueba");
            assertThat(ex.getMessage()).isEqualTo("mensaje de prueba");
            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("InvalidPatientDataException propaga el mensaje tal cual")
        void invalidPatientDataExceptionPropagatesMessage() {
            var ex = new InvalidPatientDataException("mensaje de prueba");
            assertThat(ex.getMessage()).isEqualTo("mensaje de prueba");
            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("InvalidVitalSignException propaga el mensaje tal cual")
        void invalidVitalSignExceptionPropagatesMessage() {
            var ex = new InvalidVitalSignException("mensaje de prueba");
            assertThat(ex.getMessage()).isEqualTo("mensaje de prueba");
            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("InvalidExerciseLogException propaga el mensaje tal cual")
        void invalidExerciseLogExceptionPropagatesMessage() {
            var ex = new InvalidExerciseLogException("mensaje de prueba");
            assertThat(ex.getMessage()).isEqualTo("mensaje de prueba");
            assertThat(ex).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("RateLimitExceededException propaga el mensaje tal cual")
        void rateLimitExceededExceptionPropagatesMessage() {
            var ex = new RateLimitExceededException("mensaje de prueba");
            assertThat(ex.getMessage()).isEqualTo("mensaje de prueba");
            assertThat(ex).isInstanceOf(DomainException.class);
        }
    }

    @Nested
    @DisplayName("Excepciones que construyen el mensaje a partir de un id")
    class IdBasedMessageExceptions {

        @Test
        @DisplayName("GlucoseReadingNotFoundException incluye el id en el mensaje")
        void glucoseReadingNotFoundExceptionIncludesId() {
            UUID id = UUID.randomUUID();
            var ex = new GlucoseReadingNotFoundException(id.toString());

            assertThat(ex.getMessage()).contains(id.toString());
            assertThat(ex.getMessage()).contains("no encontrada");
        }

        @Test
        @DisplayName("PatientNotFoundException incluye el id en el mensaje")
        void patientNotFoundExceptionIncludesId() {
            UUID id = UUID.randomUUID();
            var ex = new PatientNotFoundException(id.toString());

            assertThat(ex.getMessage()).contains(id.toString());
            assertThat(ex.getMessage()).contains("no encontrado");
        }
    }

    @Nested
    @DisplayName("InvalidRefreshTokenException")
    class InvalidRefreshTokenExceptionTest {

        @Test
        @DisplayName("tiene un mensaje fijo sin necesidad de parámetros")
        void hasFixedMessage() {
            var ex = new InvalidRefreshTokenException();

            assertThat(ex.getMessage()).isNotBlank();
            assertThat(ex.getMessage()).containsIgnoringCase("inválido");
            assertThat(ex).isInstanceOf(DomainException.class);
        }
    }

    @Nested
    @DisplayName("OpenCycleConflictException")
    class OpenCycleConflictExceptionTest {

        @Test
        @DisplayName("construye el mensaje incluyendo la fecha del ciclo abierto")
        void buildsMessageIncludingOpenCycleStartDate() {
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            var ex = new OpenCycleConflictException(startDate);

            assertThat(ex.getMessage()).contains("2026-06-01");
            assertThat(ex.getMessage()).contains("Indica cuándo terminó");
        }

        @Test
        @DisplayName("expone la fecha de inicio del ciclo abierto como campo propio")
        void exposesOpenCycleStartDateAsField() {
            LocalDate startDate = LocalDate.of(2026, 6, 1);
            var ex = new OpenCycleConflictException(startDate);

            assertThat(ex.getOpenCycleStartDate()).isEqualTo(startDate);
        }

        @Test
        @DisplayName("es una DomainException")
        void isDomainException() {
            var ex = new OpenCycleConflictException(LocalDate.now());
            assertThat(ex).isInstanceOf(DomainException.class);
        }
    }

    @Nested
    @DisplayName("UnauthorizedResourceAccessException")
    class UnauthorizedResourceAccessExceptionTest {

        @Test
        @DisplayName("propaga el mensaje tal cual")
        void propagatesMessage() {
            var ex = new UnauthorizedResourceAccessException("acceso no autorizado");
            assertThat(ex.getMessage()).isEqualTo("acceso no autorizado");
        }

        @Test
        @DisplayName("extiende RuntimeException directamente, no DomainException")
        void extendsRuntimeExceptionDirectly() {
            var ex = new UnauthorizedResourceAccessException("acceso no autorizado");

            assertThat(ex).isInstanceOf(RuntimeException.class);
            assertThat(ex).isNotInstanceOf(DomainException.class);
        }
    }

    @Nested
    @DisplayName("DomainException (clase base abstracta)")
    class DomainExceptionBase {

        @Test
        @DisplayName("todas las excepciones de dominio comparten la misma clase base")
        void allDomainExceptionsShareSameBase() {
            assertThat(new InvalidGlucoseReadingException("x")).isInstanceOf(DomainException.class);
            assertThat(new InvalidMealEntryException("x")).isInstanceOf(DomainException.class);
            assertThat(new InvalidMedicationException("x")).isInstanceOf(DomainException.class);
            assertThat(new InvalidPatientDataException("x")).isInstanceOf(DomainException.class);
            assertThat(new InvalidVitalSignException("x")).isInstanceOf(DomainException.class);
            assertThat(new InvalidExerciseLogException("x")).isInstanceOf(DomainException.class);
            assertThat(new RateLimitExceededException("x")).isInstanceOf(DomainException.class);
            assertThat(new GlucoseReadingNotFoundException("x")).isInstanceOf(DomainException.class);
            assertThat(new PatientNotFoundException("x")).isInstanceOf(DomainException.class);
            assertThat(new InvalidRefreshTokenException()).isInstanceOf(DomainException.class);
            assertThat(new OpenCycleConflictException(LocalDate.now())).isInstanceOf(DomainException.class);
        }

        @Test
        @DisplayName("DomainException es a su vez una RuntimeException sin verificación obligatoria")
        void domainExceptionIsRuntimeException() {
            var ex = new InvalidPatientDataException("x");
            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}