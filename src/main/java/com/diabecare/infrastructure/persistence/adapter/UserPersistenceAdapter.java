package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.infrastructure.persistence.entity.UserEntity;
import com.diabecare.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements SaveUserPort {

    private final UserJpaRepository repository;

    @Override
    public UserRecord save(String email, String encodedPassword, String role) {
        UserEntity entity = UserEntity.builder()
                .email(email)
                .password(encodedPassword)
                .role(role)
                .enabled(true)
                .build();
        UserEntity saved = repository.save(entity);
        return new UserRecord(saved.getId(), saved.getEmail(), saved.getRole());
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }
}