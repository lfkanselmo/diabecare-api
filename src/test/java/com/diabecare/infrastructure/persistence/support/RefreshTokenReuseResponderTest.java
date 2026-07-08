package com.diabecare.infrastructure.persistence.support;

import com.diabecare.infrastructure.persistence.repository.RefreshTokenJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenReuseResponder")
class RefreshTokenReuseResponderTest {

    @Mock
    private RefreshTokenJpaRepository repository;

    private RefreshTokenReuseResponder responder;
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        responder = new RefreshTokenReuseResponder(repository);
    }

    @Test
    @DisplayName("delega la revocación masiva al repositorio")
    void delegatesMassRevocationToRepository() {
        responder.revokeAllForUser(userId);

        verify(repository).revokeAllActiveByUserId(eq(userId), any(LocalDateTime.class));
    }
}
