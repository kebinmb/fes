package com.faculty_evaluation_backend.fes.dto.dashboard;

import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;

import java.math.BigDecimal;

public interface FacultyWorkloadCoverageProjection {
    String getFacultyId();

    String getFirstname();

    String getMiddlename();

    String getLastname();

    String getPosition();

    College getCollege();

    Status getStatus();

    Double getLoadLimit();

    Long getWorkloadCount();

    BigDecimal getTotalHoursPerWeek();

    Integer getNumberOfPreparations();
}
