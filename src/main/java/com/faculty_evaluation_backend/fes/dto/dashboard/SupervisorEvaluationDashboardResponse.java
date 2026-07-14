package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record SupervisorEvaluationDashboardResponse(
        String facultyId,
        String facultyName,
        String position,
        String college,
        String legacyDatabase,
        String campus,
        Long assignedClassCount,
        Boolean supervisorEvaluated,
        Long supervisorEvaluationCount,
        String supervisorIds,
        String supervisorNames,
        String supervisorPositions,
        BigDecimal supervisorAverageScore,
        Instant lastEvaluatedAt,
        Integer schoolYear,
        String semester
) {
}
