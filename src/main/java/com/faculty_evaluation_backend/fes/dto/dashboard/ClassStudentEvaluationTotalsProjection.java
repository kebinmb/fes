package com.faculty_evaluation_backend.fes.dto.dashboard;

public interface ClassStudentEvaluationTotalsProjection {
    String getClassCode();

    Long getTotalStudents();

    Long getEvaluatedStudents();
}
