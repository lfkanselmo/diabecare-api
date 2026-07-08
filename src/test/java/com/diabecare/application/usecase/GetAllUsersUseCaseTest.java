package com.diabecare.application.usecase;

import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAllUsersUseCaseImpl")
class GetAllUsersUseCaseTest {

    @Mock
    private LoadUserPort loadUserPort;

    @InjectMocks
    private GetAllUsersUseCaseImpl useCase;

    @Test
    @DisplayName("retorna la página de usuarios provista por el puerto")
    void returnsPageOfUsersFromPort() {
        Pageable pageable = PageRequest.of(0, 20);
        User user = User.builder().id(UUID.randomUUID()).email("ana@example.com").role("PATIENT").enabled(true).build();
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);
        when(loadUserPort.findAll(pageable)).thenReturn(page);

        Page<User> result = useCase.execute(pageable);

        assertThat(result.getContent()).containsExactly(user);
    }
}
