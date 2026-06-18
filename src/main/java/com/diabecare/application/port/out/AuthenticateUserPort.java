package com.diabecare.application.port.out;

public interface AuthenticateUserPort {
    void authenticate(String email, String password);
}