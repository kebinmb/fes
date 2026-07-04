package com.faculty_evaluation_backend.fes.dto.dashboard;

public interface AdminDashboardSummaryProjection {

    Long getTotalStudents();

    Long getTotalFaculty();

    Long getTotalClasses();

    Long getTotalSubjects();

    Long getTotalPrograms();

    Long getTotalSections();

    Long getExpectedEvaluations();

    Long getCompletedEvaluations();

    Long getEvaluatedStudents();

    Double getAverageOverallScore();
}
