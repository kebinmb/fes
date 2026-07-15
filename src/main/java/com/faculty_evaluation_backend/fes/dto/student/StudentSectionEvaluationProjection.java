package com.faculty_evaluation_backend.fes.dto.student;

public interface StudentSectionEvaluationProjection {
    String getProgramCode();

    String getYearLevel();

    String getSectionCode();

    Long getTotalStudents();

    Long getEvaluatedStudents();

    Long getNotYetEvaluated();
}
