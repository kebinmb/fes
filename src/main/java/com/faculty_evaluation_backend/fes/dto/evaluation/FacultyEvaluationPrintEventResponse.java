package com.faculty_evaluation_backend.fes.dto.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationPrintEventType;
import lombok.Builder;

import java.util.List;

@Builder
public record FacultyEvaluationPrintEventResponse(
        FacultyEvaluationPrintEventType type,
        int requestedCount,
        int updatedCount,
        List<String> missingReportIds
) {
}
