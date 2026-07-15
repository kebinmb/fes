package com.faculty_evaluation_backend.fes.dto.dashboard;

public interface SupervisorEvaluationDashboardMetricsProjection {
    Long getTotalFacultyCount();

    Long getEvaluatedFacultyCount();

    Long getPendingFacultyCount();
}
