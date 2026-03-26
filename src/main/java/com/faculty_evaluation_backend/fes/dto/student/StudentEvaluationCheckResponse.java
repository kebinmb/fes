package com.faculty_evaluation_backend.fes.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEvaluationCheckResponse {
    private boolean hasEvaluated;
    private String message;
}
