package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.model.User;
import com.diabecare.infrastructure.persistence.entity.UserEntity;
import com.diabecare.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<UUID> findUserIdByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(UserEntity::getId);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return userJpaRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public List<UUID> findIdsDeletedBefore(LocalDateTime cutoff) {
        return userJpaRepository.findIdsDeletedBefore(cutoff);
    }

    @Override
    public UserRecord save(String email, String encodedPassword, String role,
                            LocalDateTime termsAcceptedAt, String termsVersion) {
        UserEntity entity = UserEntity.builder()
                .email(email)
                .password(encodedPassword)
                .role(role)
                .enabled(true)
                .termsAcceptedAt(termsAcceptedAt)
                .termsVersion(termsVersion)
                .build();

        UserEntity saved = userJpaRepository.save(entity);
        return new UserRecord(saved.getId(), saved.getEmail(), saved.getRole());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public void suspend(User user) {
        userJpaRepository.findById(user.getId()).ifPresent(entity -> {
            entity.setEnabled(false);
            entity.setSuspendedAt(LocalDateTime.now());
            userJpaRepository.save(entity);
        });
    }

    @Override
    public void delete(User user) {
        userJpaRepository.findById(user.getId()).ifPresent(entity -> {
            entity.setEnabled(false);
            entity.setDeletedAt(LocalDateTime.now());
            entity.setEmail("deleted_" + user.getId() + "@diabecare.deleted");
            userJpaRepository.save(entity);
        });
    }

    @Override
    public void updateRole(UUID userId, String role) {
        userJpaRepository.findById(userId).ifPresent(entity -> {
            entity.setRole(role);
            userJpaRepository.save(entity);
        });
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userJpaRepository.findAll(pageable).map(this::toDomain);
    }

    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .role(entity.getRole())
                .enabled(entity.isEnabled())
                .suspendedAt(entity.getSuspendedAt())
                .deletedAt(entity.getDeletedAt())
                .createdAt(entity.getCreatedAt())
                .termsAcceptedAt(entity.getTermsAcceptedAt())
                .termsVersion(entity.getTermsVersion())
                .build();
    }
}