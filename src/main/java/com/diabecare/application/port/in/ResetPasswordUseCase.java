package com.diabecare.application.port.in;

public interface ResetPasswordUseCase {

    record Command(String rawToken, String newPassword) {}

    void execute(Command command);
}
