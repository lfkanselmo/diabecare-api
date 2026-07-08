package com.diabecare.application.port.in;

public interface RequestPasswordResetUseCase {

    record Command(String email, String clientIp) {}

    void execute(Command command);
}
