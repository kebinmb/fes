package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.Builder;

import java.time.Instant;

@Builder
public record FacultyEvaluationReportPrintTrackingResponse(
        String reportId,
        String facultyId,
        String facultyName,
        Integer schoolYear,
        String semester,
        Integer versionNumber,
        String status,
        String generatedByUsername,
        Instant generatedAt,
        Boolean printTrackingAvailable,
        Instant printedAt,
        String printedByUsername,
        Integer printCount,
        Instant annexDPrintedAt,
        String annexDPrintedByUsername,
        Integer annexDPrintCount
) {
}
