package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

@Builder
public record AdminDashboardSummaryResponse(
        Integer schoolYear,
        String semester,
        Long totalStudents,
        Long totalFaculty,
        Long totalClasses,
        Long totalSubjects,
        Long totalPrograms,
        Long totalSections,
        Long expectedEvaluations,
        Long completedEvaluations,
        Long evaluatedStudents,
        Long pendingEvaluations,
        Double evaluationCompletionRate,
        Double averageOverallScore
) {
}
