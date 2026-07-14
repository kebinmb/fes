package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

import java.util.List;

@Builder
public record SupervisorEvaluationDashboardPageResponse(
        List<SupervisorEvaluationDashboardResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        long totalFacultyCount,
        long evaluatedFacultyCount,
        long pendingFacultyCount
) {
}
