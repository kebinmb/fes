package com.faculty_evaluation_backend.fes.exceptions;

import com.faculty_evaluation_backend.fes.dto.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRateLimit(
            RateLimitExceededException ex,
            HttpServletRequest request
    )
    {
        return buildResponse(ex, HttpStatus.TOO_MANY_REQUESTS, request);
    }
    private ResponseEntity<ApiErrorResponse> buildResponse(
            Exception ex,
            HttpStatus status,
            HttpServletRequest request)
    {
        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(response,status);
    }
}
