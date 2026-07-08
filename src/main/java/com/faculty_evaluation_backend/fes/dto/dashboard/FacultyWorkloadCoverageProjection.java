package com.faculty_evaluation_backend.fes.dto.dashboard;

import java.math.BigDecimal;

public interface FacultyWorkloadCoverageProjection {
    String getFacultyId();

    String getFirstname();

    String getMiddlename();

    String getLastname();

    String getPosition();

    String getCollege();

    String getStatus();

    Double getLoadLimit();

    Long getWorkloadCount();

    BigDecimal getTotalHoursPerWeek();

    Integer getNumberOfPreparations();
}
