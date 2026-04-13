package com.faculty_evaluation_backend.fes.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationStatusResponse {
    private boolean success;
    private String message;
    private Long evaluationId;
    private String facultyId;
    private String evaluatorId;
    private Double overallScore;
    private String interpretation;
    private String breakdown;
    private Boolean passed;
    private LocalDateTime submittedAt;
    private String error;
    public static EvaluationStatusResponse error(String errorMessage) {
        return EvaluationStatusResponse.builder()
                .success(false)
                .message("Evaluation submission failed")
                .error(errorMessage)
                .build();
    }
}
