package com.faculty_evaluation_backend.fes.dto.authentication;

import lombok.Builder;

import java.time.Instant;

@Builder
public record StudentAccessCodeResponse(
        String studentId,
        Instant expiresAt,
        String message
) {
}
