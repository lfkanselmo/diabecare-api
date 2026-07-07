package com.diabecare.presentation.util;

import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.domain.model.DiabetesType;
import com.diabecare.domain.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CurrentUserResolver")
class CurrentUserResolverTest {

    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private LoadCaregiverLinkPort loadCaregiverLinkPort;

    private CurrentUserResolver resolver;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        resolver = new CurrentUserResolver(loadUserPort, loadPatientPort, loadCaregiverLinkPort);
    }

    @Nested
    @DisplayName("resolveUserId")
    class ResolveUserId {

        @Test
        @DisplayName("resuelve el userId a partir del email del principal UserDetails")
        void resolvesUserIdFromUserDetailsPrincipalEmail() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            UUID result = resolver.resolveUserId(auth);

            assertThat(result).isEqualTo(userId);
        }

        @Test
        @DisplayName("usa authentication.getName() cuando el principal no es UserDetails")
        void usesAuthenticationNameWhenPrincipalIsNotUserDetails() {
            Authentication auth = mock(Authentication.class);
            when(auth.getPrincipal()).thenReturn("ana@example.com");
            when(auth.getName()).thenReturn("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            UUID result = resolver.resolveUserId(auth);

            assertThat(result).isEqualTo(userId);
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando el email no corresponde a ningún usuario")
        void throwsWhenEmailDoesNotMatchAnyUser() {
            Authentication auth = authenticationWithUserDetails("noexiste@example.com");
            when(loadUserPort.findUserIdByEmail("noexiste@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resolver.resolveUserId(auth))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);
        }
    }

    @Nested
    @DisplayName("verifyOwnsPatient")
    class VerifyOwnsPatient {

        @Test
        @DisplayName("no lanza excepción cuando el paciente pertenece al usuario autenticado")
        void doesNotThrowWhenPatientBelongsToAuthenticatedUser() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            Patient patient = validPatient(userId);
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            assertThatCode(() -> resolver.verifyOwnsPatient(patient.getPatientId(), auth))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando el paciente pertenece a otro usuario")
        void throwsWhenPatientBelongsToAnotherUser() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            UUID otherUserId = UUID.randomUUID();
            Patient patient = validPatient(otherUserId);
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            assertThatThrownBy(() -> resolver.verifyOwnsPatient(patient.getPatientId(), auth))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando el paciente no existe")
        void throwsWhenPatientDoesNotExist() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            UUID nonExistentPatientId = UUID.randomUUID();
            when(loadPatientPort.findById(nonExistentPatientId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> resolver.verifyOwnsPatient(nonExistentPatientId, auth))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);
        }
    }

    @Nested
    @DisplayName("verifyCanReadPatient")
    class VerifyCanReadPatient {

        @Test
        @DisplayName("no lanza excepción cuando el paciente pertenece al usuario autenticado")
        void doesNotThrowWhenPatientBelongsToAuthenticatedUser() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            Patient patient = validPatient(userId);
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));

            assertThatCode(() -> resolver.verifyCanReadPatient(patient.getPatientId(), auth))
                    .doesNotThrowAnyException();
            verifyNoInteractions(loadCaregiverLinkPort);
        }

        @Test
        @DisplayName("no lanza excepción cuando el usuario tiene un enlace de cuidador activo")
        void doesNotThrowWhenUserHasActiveCaregiverLink() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            UUID otherUserId = UUID.randomUUID();
            Patient patient = validPatient(otherUserId);
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));
            when(loadCaregiverLinkPort.existsActive(patient.getPatientId(), userId)).thenReturn(true);

            assertThatCode(() -> resolver.verifyCanReadPatient(patient.getPatientId(), auth))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando no es dueño ni cuidador activo")
        void throwsWhenNeitherOwnerNorActiveCaregiver() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            UUID otherUserId = UUID.randomUUID();
            Patient patient = validPatient(otherUserId);
            when(loadPatientPort.findById(patient.getPatientId())).thenReturn(Optional.of(patient));
            when(loadCaregiverLinkPort.existsActive(patient.getPatientId(), userId)).thenReturn(false);

            assertThatThrownBy(() -> resolver.verifyCanReadPatient(patient.getPatientId(), auth))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);
        }
    }

    @Nested
    @DisplayName("verifyIsCurrentUser")
    class VerifyIsCurrentUser {

        @Test
        @DisplayName("no lanza excepción cuando el userId coincide con el usuario autenticado")
        void doesNotThrowWhenUserIdMatchesAuthenticatedUser() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            assertThatCode(() -> resolver.verifyIsCurrentUser(userId, auth))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("lanza UnauthorizedResourceAccessException cuando el userId no coincide")
        void throwsWhenUserIdDoesNotMatch() {
            Authentication auth = authenticationWithUserDetails("ana@example.com");
            when(loadUserPort.findUserIdByEmail("ana@example.com")).thenReturn(Optional.of(userId));

            UUID differentUserId = UUID.randomUUID();

            assertThatThrownBy(() -> resolver.verifyIsCurrentUser(differentUserId, auth))
                    .isInstanceOf(UnauthorizedResourceAccessException.class);
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Authentication authenticationWithUserDetails(String email) {
        UserDetails userDetails = new User(email, "hash", true, true, true, true,
                Collections.singletonList(() -> "ROLE_PATIENT"));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private Patient validPatient(UUID ownerUserId) {
        return Patient.create(
                ownerUserId, "Ana García", LocalDate.of(1990, 5, 10),
                DiabetesType.TYPE_1, LocalDate.of(2010, 1, 1), BigDecimal.valueOf(165));
    }
}