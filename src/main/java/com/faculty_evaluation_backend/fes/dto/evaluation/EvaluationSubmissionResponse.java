package com.faculty_evaluation_backend.fes.dto.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EvaluationSubmissionResponse(
        Long facultyEvaluationScoreId,
        Long evaluationId,
        String facultyId,
        String evaluatorId,
        String classCode,
        String subjectCode,
        String semester,
        Integer schoolYear,
        Double overallScore,
        Double overallAverageScore,
        String overallInterpretation,
        String interpretation,
        boolean success,
        String message,
        Instant createdAt,
        Instant submittedAt
) {
    public static EvaluationSubmissionResponse from(FacultyEvaluationScore score) {
        return EvaluationSubmissionResponse.builder()
                .facultyEvaluationScoreId(score.getFacultyEvaluationScoreId())
                .evaluationId(score.getFacultyEvaluationScoreId())
                .facultyId(score.getFacultyId())
                .evaluatorId(score.getEvaluatorId())
                .classCode(score.getClassCode())
                .subjectCode(score.getSubjectCode())
                .semester(score.getSemester())
                .schoolYear(score.getSchoolYear())
                .overallScore(score.getOverallAverageScore())
                .overallAverageScore(score.getOverallAverageScore())
                .overallInterpretation(score.getOverallInterpretation())
                .interpretation(score.getOverallInterpretation())
                .success(true)
                .message("Evaluation submitted successfully.")
                .createdAt(score.getCreatedAt())
                .submittedAt(score.getCreatedAt())
                .build();
    }
}
