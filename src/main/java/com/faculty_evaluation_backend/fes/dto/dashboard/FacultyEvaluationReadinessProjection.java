package com.faculty_evaluation_backend.fes.dto.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

public interface FacultyEvaluationReadinessProjection {
    String getFacultyId();

    String getFacultyName();

    String getPosition();

    String getCollege();

    String getLegacyDatabase();

    String getCampus();

    String getSubjects();

    Long getStudentEvaluationCount();

    Long getSupervisorEvaluationCount();

    Long getTotalScoreRecords();

    BigDecimal getSetAverage();

    BigDecimal getSefAverage();

    BigDecimal getOverallAverage();

    Instant getLastEvaluatedAt();
}