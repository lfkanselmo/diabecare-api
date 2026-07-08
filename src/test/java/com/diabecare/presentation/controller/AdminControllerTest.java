package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.ChangeUserRoleUseCase;
import com.diabecare.application.port.in.GetAllUsersUseCase;
import com.diabecare.domain.model.User;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController")
class AdminControllerTest {

    @Mock private GetAllUsersUseCase getAllUsersUseCase;
    @Mock private ChangeUserRoleUseCase changeUserRoleUseCase;
    @Mock private CurrentUserResolver currentUserResolver;
    @Mock private Authentication authentication;

    private MockMvc mockMvc;
    private final UUID currentAdminId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AdminController controller = new AdminController(
                getAllUsersUseCase, changeUserRoleUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Nested @DisplayName("GET /api/v1/admin/users")
    class GetUsers {

        @Test @DisplayName("retorna 200 con la página de usuarios mapeada correctamente")
        void returns200WithPagedUsersMappedCorrectly() throws Exception {
            User user = User.builder()
                    .id(UUID.randomUUID()).email("ana@example.com").role("PATIENT")
                    .enabled(true).createdAt(LocalDateTime.now()).build();
            var pageable = PageRequest.of(0, 20);

            when(getAllUsersUseCase.execute(pageable))
                    .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

            mockMvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].email").value("ana@example.com"))
                    .andExpect(jsonPath("$.content[0].role").value("PATIENT"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested @DisplayName("PATCH /api/v1/admin/users/{userId}/role")
    class ChangeUserRole {

        @Test @DisplayName("retorna 204 y ejecuta el cambio de rol cuando el objetivo no es el usuario actual")
        void returns204AndExecutesRoleChangeWhenTargetIsNotCurrentUser() throws Exception {
            UUID targetUserId = UUID.randomUUID();
            when(currentUserResolver.resolveUserId(any())).thenReturn(currentAdminId);

            mockMvc.perform(patch("/api/v1/admin/users/{userId}/role", targetUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"ADMIN\"}"))
                    .andExpect(status().isNoContent());

            verify(changeUserRoleUseCase).execute(
                    new ChangeUserRoleUseCase.Command(targetUserId, "ADMIN"));
        }

        @Test @DisplayName("retorna 400 cuando un admin intenta cambiar su propio rol")
        void returns400WhenAdminTriesToChangeOwnRole() throws Exception {
            when(currentUserResolver.resolveUserId(any())).thenReturn(currentAdminId);

            mockMvc.perform(patch("/api/v1/admin/users/{userId}/role", currentAdminId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"PATIENT\"}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(changeUserRoleUseCase);
        }

        @Test @DisplayName("retorna 400 cuando el rol enviado está en blanco")
        void returns400WhenRoleIsBlank() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/users/{userId}/role", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"\"}"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(changeUserRoleUseCase);
        }
    }
}
