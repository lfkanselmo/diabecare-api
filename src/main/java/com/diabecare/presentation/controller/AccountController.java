package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.DeleteAccountUseCase;
import com.diabecare.application.port.in.SuspendAccountUseCase;
import com.diabecare.presentation.util.CurrentUserResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Tag(name = "Cuenta de usuario")
public class AccountController {

    private final SuspendAccountUseCase suspendAccountUseCase;
    private final DeleteAccountUseCase  deleteAccountUseCase;
    private final CurrentUserResolver   currentUserResolver;

    @PatchMapping("/{userId}/suspend")
    public ResponseEntity<Void> suspend(@PathVariable UUID userId, Authentication authentication) {
        currentUserResolver.verifyIsCurrentUser(userId, authentication);
        suspendAccountUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId, Authentication authentication) {
        currentUserResolver.verifyIsCurrentUser(userId, authentication);
        deleteAccountUseCase.execute(userId);
        return ResponseEntity.noContent().build();
    }
}