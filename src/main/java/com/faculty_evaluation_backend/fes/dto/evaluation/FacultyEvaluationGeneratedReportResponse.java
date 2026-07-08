package com.faculty_evaluation_backend.fes.dto.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationReportStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record FacultyEvaluationGeneratedReportResponse(
        String reportId,
        String facultyId,
        String facultyName,
        Integer schoolYear,
        String semester,
        Integer versionNumber,
        FacultyEvaluationReportStatus status,
        String reportHash,
        String verificationUrl,
        String qrCodeDataUri,
        Long generatedByUserId,
        String generatedByUsername,
        Instant generatedAt,
        List<FacultyEvaluationPrintResponse> items
) {
}
