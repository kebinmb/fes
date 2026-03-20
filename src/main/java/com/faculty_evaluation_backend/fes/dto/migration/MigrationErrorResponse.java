package com.faculty_evaluation_backend.fes.dto.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationErrorResponse {
    private String entity;
    private String entityId;
    private String errorType;
    private String message;
    private Instant timestamp;
}
