package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ChangeUserRoleUseCase;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.exception.InvalidRoleException;
import com.diabecare.domain.exception.UserNotFoundException;
import com.diabecare.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeUserRoleUseCaseImpl")
class ChangeUserRoleUseCaseTest {

    @Mock
    private LoadUserPort loadUserPort;
    @Mock
    private SaveUserPort saveUserPort;

    @InjectMocks
    private ChangeUserRoleUseCaseImpl useCase;

    private final UUID userId = UUID.randomUUID();

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("actualiza el rol cuando el usuario existe y el rol es válido")
        void updatesRoleWhenUserExistsAndRoleIsValid() {
            when(loadUserPort.findById(userId)).thenReturn(Optional.of(validUser()));

            useCase.execute(new ChangeUserRoleUseCase.Command(userId, "ADMIN"));

            verify(saveUserPort).updateRole(userId, "ADMIN");
        }

        @Test
        @DisplayName("lanza InvalidRoleException cuando el rol no es PATIENT ni ADMIN")
        void throwsInvalidRoleExceptionWhenRoleIsNotAllowed() {
            assertThatThrownBy(() -> useCase.execute(new ChangeUserRoleUseCase.Command(userId, "SUPERUSER")))
                    .isInstanceOf(InvalidRoleException.class);

            verifyNoInteractions(loadUserPort, saveUserPort);
        }

        @Test
        @DisplayName("lanza UserNotFoundException cuando el usuario no existe")
        void throwsUserNotFoundExceptionWhenUserDoesNotExist() {
            when(loadUserPort.findById(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.execute(new ChangeUserRoleUseCase.Command(userId, "ADMIN")))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(saveUserPort);
        }
    }

    private User validUser() {
        return User.builder().id(userId).email("ana@example.com").role("PATIENT").enabled(true).build();
    }
}
