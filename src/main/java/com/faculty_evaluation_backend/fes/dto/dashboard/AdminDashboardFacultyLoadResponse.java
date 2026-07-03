package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

@Builder
public record AdminDashboardFacultyLoadResponse(
        String facultyId,
        String facultyName,
        Long totalClasses,
        Long totalSubjects,
        Long totalStudents,
        Long completedEvaluations,
        Double averageOverallScore
) {
}
