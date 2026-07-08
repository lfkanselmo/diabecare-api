package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ChangeUserRoleUseCase;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.exception.InvalidRoleException;
import com.diabecare.domain.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class ChangeUserRoleUseCaseImpl implements ChangeUserRoleUseCase {

    private static final Set<String> VALID_ROLES = Set.of("PATIENT", "ADMIN");

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;

    @Override
    public void execute(Command command) {
        if (!VALID_ROLES.contains(command.newRole())) {
            throw new InvalidRoleException(
                    "Rol inválido: " + command.newRole() + ". Valores permitidos: " + VALID_ROLES);
        }

        loadUserPort.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId().toString()));

        saveUserPort.updateRole(command.userId(), command.newRole());
    }
}
