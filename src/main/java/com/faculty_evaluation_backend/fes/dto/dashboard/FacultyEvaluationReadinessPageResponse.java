package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record FacultyEvaluationReadinessPageResponse(
        List<FacultyEvaluationReadinessResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        long totalReadyFacultyCount,
        long totalStudentEvaluationCount,
        long totalSupervisorEvaluationCount,
        long totalScoreRecordCount,
        BigDecimal averageOverallScore
) {
}