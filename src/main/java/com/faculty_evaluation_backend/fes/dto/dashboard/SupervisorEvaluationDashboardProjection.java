package com.faculty_evaluation_backend.fes.dto.dashboard;

import java.math.BigDecimal;
import java.time.Instant;

public interface SupervisorEvaluationDashboardProjection {
    String getFacultyId();

    String getFacultyName();

    String getPosition();

    String getCollege();

    String getLegacyDatabase();

    String getCampus();

    Long getAssignedClassCount();

    Long getSupervisorEvaluationCount();

    String getSupervisorIds();

    String getSupervisorNames();

    String getSupervisorPositions();

    BigDecimal getSupervisorAverageScore();

    Instant getLastEvaluatedAt();
}
