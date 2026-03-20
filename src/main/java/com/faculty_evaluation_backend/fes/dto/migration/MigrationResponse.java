package com.faculty_evaluation_backend.fes.dto.migration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MigrationResponse<T> {
    private String status;
    private String message;
    private Instant startTime;
    private Instant endTime;
    private Long durationMs;
    private MigrationStatistics stats;
    private T data;
    private List<MigrationErrorResponse> errors;
}
