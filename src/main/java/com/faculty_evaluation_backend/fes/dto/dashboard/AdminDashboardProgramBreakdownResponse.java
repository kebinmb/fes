package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

@Builder
public record AdminDashboardProgramBreakdownResponse(
        String programCode,
        Long totalStudents,
        Long totalClasses,
        Long totalSections,
        Long expectedEvaluations,
        Long completedEvaluations,
        Double completionRate,
        Double averageOverallScore
) {
}
