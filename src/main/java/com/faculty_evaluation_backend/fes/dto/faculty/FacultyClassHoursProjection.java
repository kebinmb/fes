package com.faculty_evaluation_backend.fes.dto.faculty;

public interface FacultyClassHoursProjection {
    Double getLecUnits();
    Double getLabUnits();
    Double getLecHours();
    Double getLabHours();
    Double getTotalHoursPerWeek();
    Double getTotalTeachingLoad();
}
