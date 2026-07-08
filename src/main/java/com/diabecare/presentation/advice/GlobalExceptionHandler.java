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

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(
            UserNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(OpenCycleConflictException.class)
    public ResponseEntity<ApiError> handleOpenCycleConflict(
            OpenCycleConflictException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, "OPEN_CYCLE_CONFLICT", ex.getMessage(), request);
    }

    @ExceptionHandler({
            InvalidPatientDataException.class,
            InvalidGlucoseReadingException.class,
            InvalidMealEntryException.class,
            InvalidVitalSignException.class,
            InvalidMedicationException.class,
            InvalidExerciseLogException.class,
            InvalidCaregiverInviteException.class,
            InvalidRoleException.class
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

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiError> handleRateLimit(
            RateLimitExceededException ex, HttpServletRequest request) {
        return build(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ApiError> handleInvalidRefreshToken(
            InvalidRefreshTokenException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedResourceAccessException.class)
    public ResponseEntity<ApiError> handleUnauthorizedResourceAccess(
            UnauthorizedResourceAccessException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "UNAUTHORIZED_RESOURCE_ACCESS", ex.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "No tienes permiso para realizar esta acción.", request);
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(
            org.springframework.security.authentication.DisabledException ex,
            HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "ACCOUNT_SUSPENDED",
                "Tu cuenta está suspendida. Contacta soporte para reactivarla.", request);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex,
            HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS",
                "Correo o contraseña incorrectos.", request);
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