package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.Builder;

import java.util.List;

@Builder
public record FacultyEvaluationBulkReportResponse(
        Integer requestedCount,
        Integer generatedCount,
        List<String> skippedFacultyIds,
        List<FacultyEvaluationGeneratedReportResponse> reports
) {
}
