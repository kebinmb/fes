package com.faculty_evaluation_backend.fes.dto.dashboard;

public interface AdminDashboardProgramBreakdownProjection {

    String getProgramCode();

    Long getTotalStudents();

    Long getTotalClasses();

    Long getTotalSections();

    Long getExpectedEvaluations();

    Long getCompletedEvaluations();

    Double getAverageOverallScore();
}
