package com.faculty_evaluation_backend.fes.dto.response;

import com.faculty_evaluation_backend.fes.entities.evaluation.CommitmentAndTransparency;
import com.faculty_evaluation_backend.fes.entities.evaluation.ContentKnowledgePedagogyAndTechnology;
import com.faculty_evaluation_backend.fes.entities.evaluation.ManagementOfTeachingAndLearning;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FetchFacultyEvaluationScoreResponse {

    private Long facultyEvaluationScoreId;
    private String facultyId;
    private String facultyName;
    private String position;
    private String evaluatorId;
    private String classCode;
    private String semester;
    private String schoolYear;
    private String subjectCode;
    private String yearLevel;

    private String commentsOrFeedbacks;

    private Double overallAverageScore;
    private String overallInterpretation;
}
