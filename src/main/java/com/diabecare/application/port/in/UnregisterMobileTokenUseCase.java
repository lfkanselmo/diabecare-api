package com.diabecare.application.port.in;

public interface UnregisterMobileTokenUseCase {

    record Command(String deviceToken) {}

    void execute(Command command);
}
