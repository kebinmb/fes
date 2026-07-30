package com.faculty_evaluation_backend.fes.dto.evaluation;

import java.time.Instant;

public interface FacultyEvaluationReportPrintTrackingProjection {
    String getReportId();

    String getFacultyId();

    String getFacultyName();

    Integer getSchoolYear();

    String getSemester();

    Integer getVersionNumber();

    String getStatus();

    String getGeneratedByUsername();

    Instant getGeneratedAt();

    Boolean getPrintTrackingAvailable();

    Instant getPrintedAt();

    String getPrintedByUsername();

    Integer getPrintCount();

    Instant getAnnexDPrintedAt();

    String getAnnexDPrintedByUsername();

    Integer getAnnexDPrintCount();
}
