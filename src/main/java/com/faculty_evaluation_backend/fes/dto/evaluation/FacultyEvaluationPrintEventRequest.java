package com.faculty_evaluation_backend.fes.dto.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationPrintEventType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FacultyEvaluationPrintEventRequest(
        @NotEmpty List<String> reportIds,
        @NotNull FacultyEvaluationPrintEventType type
) {
}
