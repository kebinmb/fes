package com.faculty_evaluation_backend.fes.dto.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationReportStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record FacultyEvaluationReportVerificationResponse(
        String reportId,
        String facultyId,
        String facultyName,
        Integer schoolYear,
        String semester,
        Integer versionNumber,
        FacultyEvaluationReportStatus status,
        String reportHash,
        Instant generatedAt,
        String generatedByUsername,
        String message
) {
}
