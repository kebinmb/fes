package com.faculty_evaluation_backend.fes.services.data.evaluation.strategies;

import com.faculty_evaluation_backend.fes.entities.primary.enums.EvaluationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvaluationStrategyFactory {
    private final StudentEvaluationStrategy studentEvaluationStrategy;
    private final SupervisorEvaluationStrategy supervisorEvaluationStrategy;

    public EvaluationStrategy getStrategy(EvaluationType evaluationType){
        return switch (evaluationType){
            case ROLE_STUDENT -> studentEvaluationStrategy;
            case ROLE_DEAN -> supervisorEvaluationStrategy;
            case ROLE_PROGRAM_CHAIR -> supervisorEvaluationStrategy;
        };
    }
}
