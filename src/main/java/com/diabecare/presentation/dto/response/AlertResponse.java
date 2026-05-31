package com.diabecare.presentation.dto.response;

public record AlertResponse(
        String type,
        String severity,
        String title,
        String message
) {}