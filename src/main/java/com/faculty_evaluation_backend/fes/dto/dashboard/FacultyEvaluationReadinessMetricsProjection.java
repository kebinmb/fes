package com.faculty_evaluation_backend.fes.dto.dashboard;

import java.math.BigDecimal;

public interface FacultyEvaluationReadinessMetricsProjection {
    Long getTotalReadyFacultyCount();

    Long getTotalStudentEvaluationCount();

    Long getTotalSupervisorEvaluationCount();

    Long getTotalScoreRecordCount();

    BigDecimal getAverageOverallScore();
}