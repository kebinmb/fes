package com.faculty_evaluation_backend.fes.dto.evaluation;

public interface StudentEvaluationStatusProjection {
    String getStudentId();
    String getProgramCode();
    String getYearLevel();
    String getSectionCode();
    String getSubjectCode();
    String getCreatedAt();
    String getEvaluationStatus();
}
