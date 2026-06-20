package com.diabecare.application.port.in;

public interface LogoutCurrentSessionUseCase {

    record Command(String refreshToken) {}

    void execute(Command command);
}