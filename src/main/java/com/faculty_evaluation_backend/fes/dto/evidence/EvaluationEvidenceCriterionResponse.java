package com.faculty_evaluation_backend.fes.dto.evidence;

import com.faculty_evaluation_backend.fes.entities.evaluation.enums.EvaluationEvidenceCriterion;
import lombok.Builder;

@Builder
public record EvaluationEvidenceCriterionResponse(
        String name,
        String category,
        String label,
        String ratingKey
) {
    public static EvaluationEvidenceCriterionResponse from(
            EvaluationEvidenceCriterion criterion
    ) {
        return EvaluationEvidenceCriterionResponse.builder()
                .name(criterion.name())
                .category(criterion.getCategory())
                .label(criterion.getLabel())
                .ratingKey(criterion.getRatingKey())
                .build();
    }
}
