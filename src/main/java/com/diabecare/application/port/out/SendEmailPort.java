package com.diabecare.application.port.out;

public interface SendEmailPort {
    // El adaptador construye el enlace completo (necesita la URL base del
    // frontend, un detalle de infraestructura que el caso de uso no debe conocer).
    void sendPasswordResetEmail(String toEmail, String rawResetToken);
}
