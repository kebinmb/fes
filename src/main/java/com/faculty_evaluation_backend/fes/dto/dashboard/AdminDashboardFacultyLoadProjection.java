package com.faculty_evaluation_backend.fes.dto.dashboard;

public interface AdminDashboardFacultyLoadProjection {

    String getFacultyId();

    String getFacultyName();

    Long getTotalClasses();

    Long getTotalSubjects();

    Long getTotalStudents();

    Long getCompletedEvaluations();

    Double getAverageOverallScore();
}
