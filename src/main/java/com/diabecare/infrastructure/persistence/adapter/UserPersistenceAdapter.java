package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.infrastructure.persistence.entity.UserEntity;
import com.diabecare.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements LoadUserPort, SaveUserPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<UUID> findUserIdByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(user -> user.getId());
    }

    @Override
    public UserRecord save(String email, String encodedPassword, String role) {
        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .password(encodedPassword)
                .role(role)
                .enabled(true)
                .build();

        UserEntity savedEntity = userJpaRepository.save(userEntity);

        return new UserRecord(
                savedEntity.getId(),
                savedEntity.getEmail(),
                savedEntity.getRole()
        );
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}