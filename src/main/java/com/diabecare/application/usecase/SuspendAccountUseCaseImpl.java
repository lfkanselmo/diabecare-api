package com.diabecare.application.usecase;

import com.diabecare.application.port.in.SuspendAccountUseCase;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class SuspendAccountUseCaseImpl implements SuspendAccountUseCase {

    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;

    @Override
    public void execute(UUID userId) {
        var user = loadUserPort.findById(userId)
                .orElseThrow(() -> new InvalidPatientDataException(
                        "Usuario no encontrado: " + userId));

        if (user.isDeleted()) throw new InvalidPatientDataException(
                "La cuenta ya fue eliminada");

        if (user.isSuspended()) throw new InvalidPatientDataException(
                "La cuenta ya está suspendida");

        saveUserPort.suspend(user);
    }
}