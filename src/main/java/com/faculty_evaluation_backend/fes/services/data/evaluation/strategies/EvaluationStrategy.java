package com.faculty_evaluation_backend.fes.services.data.evaluation.strategies;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;

public interface EvaluationStrategy {
    void validate(BaseEvaluationDTO baseEvaluationDTO);
    boolean isDuplicate(BaseEvaluationDTO baseEvaluationDTO);
}
