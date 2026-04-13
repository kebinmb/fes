package com.faculty_evaluation_backend.fes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCheckResponse {
    private boolean hasEvaluated;
    private String message;
}
