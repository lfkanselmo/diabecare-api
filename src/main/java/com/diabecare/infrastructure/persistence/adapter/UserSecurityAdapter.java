package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.LoadUserSecurityPort;
import com.diabecare.infrastructure.persistence.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSecurityAdapter implements LoadUserSecurityPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<UserSecurityData> findSecurityDataByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(user -> new UserSecurityData(
                        user.getEmail(),
                        user.getPassword(),
                        user.isEnabled(),
                        user.getRole()
                ));
    }
}