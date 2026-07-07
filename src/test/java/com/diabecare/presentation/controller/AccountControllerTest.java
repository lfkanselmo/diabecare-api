package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.DeleteAccountUseCase;
import com.diabecare.application.port.in.ExportAccountDataUseCase;
import com.diabecare.application.port.in.SuspendAccountUseCase;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.presentation.advice.GlobalExceptionHandler;
import com.diabecare.presentation.util.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController")
class AccountControllerTest {

    @Mock
    private SuspendAccountUseCase suspendAccountUseCase;
    @Mock
    private DeleteAccountUseCase deleteAccountUseCase;
    @Mock
    private ExportAccountDataUseCase exportAccountDataUseCase;
    @Mock
    private CurrentUserResolver currentUserResolver;

    private MockMvc mockMvc;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        AccountController controller = new AccountController(
                suspendAccountUseCase, deleteAccountUseCase, exportAccountDataUseCase, currentUserResolver);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("PATCH /api/v1/account/{userId}/suspend")
    class Suspend {

        @Test
        @DisplayName("retorna 204 cuando la suspensión es exitosa")
        void returns204WhenSuspensionSuccessful() throws Exception {
            mockMvc.perform(patch("/api/v1/account/{userId}/suspend", userId))
                    .andExpect(status().isNoContent());

            verify(currentUserResolver).verifyIsCurrentUser(eq(userId), any());
            verify(suspendAccountUseCase).execute(userId);
        }

        @Test
        @DisplayName("retorna 403 cuando el usuario no es el dueño de la cuenta")
        void returns403WhenUserIsNotAccountOwner() throws Exception {
            doThrow(new UnauthorizedResourceAccessException("sin permiso"))
                    .when(currentUserResolver).verifyIsCurrentUser(eq(userId), any());

            mockMvc.perform(patch("/api/v1/account/{userId}/suspend", userId))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(suspendAccountUseCase);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/account/{userId}")
    class Delete {

        @Test
        @DisplayName("retorna 204 cuando la eliminación es exitosa")
        void returns204WhenDeletionSuccessful() throws Exception {
            mockMvc.perform(delete("/api/v1/account/{userId}", userId))
                    .andExpect(status().isNoContent());

            verify(deleteAccountUseCase).execute(userId);
        }

        @Test
        @DisplayName("retorna 403 cuando el usuario no es el dueño de la cuenta")
        void returns403WhenUserIsNotAccountOwner() throws Exception {
            doThrow(new UnauthorizedResourceAccessException("sin permiso"))
                    .when(currentUserResolver).verifyIsCurrentUser(eq(userId), any());

            mockMvc.perform(delete("/api/v1/account/{userId}", userId))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(deleteAccountUseCase);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/account/{userId}/export")
    class Export {

        @Test
        @DisplayName("retorna 200 con el JSON de la exportación y encabezados de descarga")
        void returns200WithExportJsonAndDownloadHeaders() throws Exception {
            byte[] json = "{\"email\":\"ana@example.com\"}".getBytes();
            when(exportAccountDataUseCase.exportAsJson(userId)).thenReturn(json);

            mockMvc.perform(get("/api/v1/account/{userId}/export", userId))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("attachment")))
                    .andExpect(content().bytes(json));
        }

        @Test
        @DisplayName("retorna 403 cuando el usuario no es el dueño de la cuenta")
        void returns403WhenUserIsNotAccountOwner() throws Exception {
            doThrow(new UnauthorizedResourceAccessException("sin permiso"))
                    .when(currentUserResolver).verifyIsCurrentUser(eq(userId), any());

            mockMvc.perform(get("/api/v1/account/{userId}/export", userId))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(exportAccountDataUseCase);
        }
    }
}