package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetAllUsersUseCase;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAllUsersUseCaseImpl implements GetAllUsersUseCase {

    private final LoadUserPort loadUserPort;

    @Override
    public Page<User> execute(Pageable pageable) {
        return loadUserPort.findAll(pageable);
    }
}
