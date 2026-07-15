package com.faculty_evaluation_backend.fes.dto.dashboard;

public record ClassStudentEvaluationStatsResponse(
        String classCode,
        String facultyId,
        String facultyName,
        String subjectCode,
        String programCode,
        String yearLevel,
        String sectionCode,
        Long totalStudents,
        Long evaluatedStudents,
        Long pendingStudents,
        Double evaluationPercentage
) {
}
