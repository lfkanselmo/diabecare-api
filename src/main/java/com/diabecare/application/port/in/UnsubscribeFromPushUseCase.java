package com.diabecare.application.port.in;

public interface UnsubscribeFromPushUseCase {

    record Command(String endpoint) {}

    void execute(Command command);
}
