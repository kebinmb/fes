package com.faculty_evaluation_backend.fes.dto.evaluation;

import java.time.LocalDateTime;

public interface StudentFacultyEvaluationProjection {
    String getStudentId();

    String getStudentFirstname();

    String getStudentLastname();

    String getClassCode();

    String getProgramCode();

    String getSectionCode();

    String getFacultyId();

    String getFacultyFirstname();

    String getFacultyLastname();

    LocalDateTime getCreatedAt();
}
