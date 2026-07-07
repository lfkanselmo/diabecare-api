package com.diabecare.presentation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 100)
        String password,

        @NotBlank @Size(max = 150)
        String fullName,

        @NotBlank
        String dateOfBirth,

        @NotBlank
        String diabetesType,

        @NotBlank
        String diagnosisDate,

        @NotBlank
        String heightCm,

        @NotBlank
        String biologicalSex,

        @AssertTrue(message = "Debes aceptar la política de tratamiento de datos personales")
        boolean termsAccepted
) {}