package com.diabecare.application.usecase;

import com.diabecare.application.dto.UserRecord;
import com.diabecare.application.port.in.RegisterUserUseCase;
import com.diabecare.application.port.out.SaveUserPort;
import com.diabecare.domain.exception.InvalidPatientDataException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private final SaveUserPort saveUserPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserRecord execute(Command command) {
        if (saveUserPort.existsByEmail(command.email())) {
            throw new InvalidPatientDataException(
                    "Ya existe una cuenta con el correo: " + command.email());
        }
        return saveUserPort.save(
                command.email(),
                passwordEncoder.encode(command.password()),
                "PATIENT"
        );
    }
}