package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.Builder;

@Builder
public record StudentEvaluationStatusResponse(
        String studentId,
        String programCode,
        String yearLevel,
        String sectionCode,
        String subjectCode,
        String createdAt,
        String evaluationStatus
) {
}
