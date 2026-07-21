package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record FacultyEvaluationReadinessResponse(
        String facultyId,
        String facultyName,
        String position,
        String college,
        String legacyDatabase,
        String campus,
        Integer schoolYear,
        String semester,
        List<String> subjects,
        Long studentEvaluationCount,
        Long supervisorEvaluationCount,
        Long totalScoreRecords,
        BigDecimal setAverage,
        BigDecimal sefAverage,
        BigDecimal overallAverage,
        Instant lastEvaluatedAt
) {
}