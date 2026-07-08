package com.diabecare.application.port.in;

import com.diabecare.domain.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GetAllUsersUseCase {
    Page<User> execute(Pageable pageable);
}
