package com.diabecare.presentation.advice;

import com.diabecare.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<ApiError> handlePatientNotFound(
            PatientNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "PATIENT_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(GlucoseReadingNotFoundException.class)
    public ResponseEntity<ApiError> handleGlucoseNotFound(
            GlucoseReadingNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "GLUCOSE_READING_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidPatientDataException.class,
            InvalidGlucoseReadingException.class,
            InvalidMealEntryException.class,
            InvalidVitalSignException.class,
            InvalidMedicationException.class
    })
    public ResponseEntity<ApiError> handleDomainValidation(
            DomainException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "DOMAIN_VALIDATION_ERROR", ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldError> fieldErrors = extractFieldErrors(ex.getBindingResult());
        ApiError error = new ApiError(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "VALIDATION_ERROR",
                "Error de validación en los datos enviados",
                request.getRequestURI(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Error interno del servidor", request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code,
                                           String message, HttpServletRequest request) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(error);
    }

    private List<ApiError.FieldError> extractFieldErrors(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(e -> new ApiError.FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
    }
}