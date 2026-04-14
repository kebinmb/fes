package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SubjectEvaluationSummary {
    private String facultyId;
    private String facultyName;
    private String facultyPosition;
    private String subjectCode;
    private String subjectTitle;
    private String semester;
    private Integer schoolYear;

    private int totalEvaluations;
    private int uniqueStudents;

    private double overallAverage;
    private double teachingAverage;
    private double contentAverage;
    private double commitmentAverage;

    private long passedCount;
    private long positiveCount;
    private String interpretation;

    public static SubjectEvaluationSummary empty(
            String facultyId, String facultyName, String subjectCode, String subjectTitle) {
        return SubjectEvaluationSummary.builder()
                .facultyId(facultyId)
                .facultyName(facultyName)
                .subjectCode(subjectCode)
                .subjectTitle(subjectTitle)
                .totalEvaluations(0)
                .uniqueStudents(0)
                .overallAverage(0.0)
                .teachingAverage(0.0)
                .contentAverage(0.0)
                .commitmentAverage(0.0)
                .passedCount(0L)
                .positiveCount(0L)
                .interpretation("No evaluations yet")
                .build();
    }
}
