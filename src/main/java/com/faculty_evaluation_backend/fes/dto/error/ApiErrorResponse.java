package com.faculty_evaluation_backend.fes.dto.error;

import java.time.Instant;


public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
