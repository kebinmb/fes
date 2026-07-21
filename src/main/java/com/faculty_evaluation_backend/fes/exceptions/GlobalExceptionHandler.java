package com.faculty_evaluation_backend.fes.exceptions;

import com.faculty_evaluation_backend.fes.dto.error.ApiErrorResponse;

import jakarta.persistence.EntityNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;

import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.io.IOException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(
            RateLimitExceededException ex,
            HttpServletRequest request
    ) {

        log.warn("Rate limit exceeded: {}", ex.getMessage());

        return buildResponse(ex, HttpStatus.TOO_MANY_REQUESTS, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn("Resource not found: {}", ex.getMessage());

        return buildResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {

        log.warn("Entity not found: {}", ex.getMessage());

        return buildResponse(
                new Exception(ex.getMessage()),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request
    ) {

        log.warn("Bad request: {}", ex.getMessage());

        return buildResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request
    ) {

        log.warn("Unauthorized access: {}", ex.getMessage());

        return buildResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidEnum(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {

        log.warn("Invalid parameter: {}", ex.getMessage());

        return buildResponse(
                new Exception("Invalid request parameter value"),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("Validation failed: {}", message);

        return buildResponse(
                new Exception(message),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));

        log.warn("Constraint violation: {}", message);

        return buildResponse(
                new Exception(message),
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @ExceptionHandler(DuplicateEvaluationException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateEvaluation(
            DuplicateEvaluationException ex,
            HttpServletRequest request
    ) {

        log.warn("Duplicate evaluation: {}", ex.getMessage());

        return buildResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {

        log.warn("Bad credentials attempt");

        ApiErrorResponse error = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    @ExceptionHandler({
            HttpMessageNotWritableException.class,
            AsyncRequestNotUsableException.class
    })
    public ResponseEntity<?> handleResponseWriteFailure(
            Exception ex,
            HttpServletRequest request
    ) {
        if (isClientAbort(ex)) {
            log.debug(
                    "Client disconnected before response completed | method={} path={}",
                    request.getMethod(),
                    request.getRequestURI()
            );
            return ResponseEntity.noContent().build();
        }

        log.error("Failed to write HTTP response", ex);

        return buildResponse(
                new Exception("Failed to write response."),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }
    @ExceptionHandler({
            DataAccessException.class,
            DataRetrievalFailureException.class
    })
    public ResponseEntity<ApiErrorResponse> handleDatabaseException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Database error", ex);

        return buildResponse(
                new Exception("Database operation failed."),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Unhandled exception", ex);

        return buildResponse(
                new Exception(
                        "Something went wrong. Please contact support."
                ),
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    private boolean isClientAbort(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {
            String className = current.getClass().getName();
            String message = current.getMessage();
            String normalizedMessage = message == null
                    ? ""
                    : message.toLowerCase();

            if (current instanceof AsyncRequestNotUsableException
                    || current instanceof IOException
                    || className.equals("org.apache.catalina.connector.ClientAbortException")) {
                if (normalizedMessage.contains("connection was aborted")
                        || normalizedMessage.contains("connection reset")
                        || normalizedMessage.contains("broken pipe")
                        || normalizedMessage.contains("failed to flush")) {
                    return true;
                }
            }

            current = current.getCause();
        }

        return false;
    }
    private ResponseEntity<ApiErrorResponse> buildResponse(
            Exception ex,
            HttpStatus status,
            HttpServletRequest request
    ) {

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(response, status);
    }
}