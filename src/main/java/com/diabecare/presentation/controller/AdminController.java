package com.diabecare.presentation.controller;

import com.diabecare.application.port.in.ChangeUserRoleUseCase;
import com.diabecare.application.port.in.GetAllUsersUseCase;
import com.diabecare.domain.exception.InvalidRoleException;
import com.diabecare.domain.model.User;
import com.diabecare.presentation.dto.request.ChangeUserRoleRequest;
import com.diabecare.presentation.dto.response.AdminUserResponse;
import com.diabecare.presentation.dto.response.PageResponse;
import com.diabecare.presentation.util.CurrentUserResolver;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración")
public class AdminController {

    private final GetAllUsersUseCase getAllUsersUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/users")
    public ResponseEntity<PageResponse<AdminUserResponse>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(PageResponse.of(
                getAllUsersUseCase.execute(PageRequest.of(page, size)),
                this::toResponse));
    }

    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<Void> changeUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeUserRoleRequest request,
            Authentication authentication) {

        UUID currentUserId = currentUserResolver.resolveUserId(authentication);
        if (userId.equals(currentUserId)) {
            throw new InvalidRoleException("No puedes cambiar tu propio rol");
        }

        changeUserRoleUseCase.execute(new ChangeUserRoleUseCase.Command(userId, request.role()));
        return ResponseEntity.noContent().build();
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                user.getSuspendedAt(),
                user.getDeletedAt(),
                user.getCreatedAt()
        );
    }
}
