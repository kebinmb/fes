package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FacultyEvaluationPrintResponse {
    private Long facultyEvaluationScoreId;
    private String facultyId;
    private String facultyName;
    private String evaluatorId;
    private String classCode;
    private Integer numberOfStudents;
    private String college;
    private String sectionCode;
    private String programCode;
    private String position;
    private String semester;
    private Integer schoolYear;
    private String subjectCode;
    private String yearLevel;
    private Double overallAverageScore;
    private String overallInterpretation;
    private Double setRating;
    private Double sefRating;
    private String evaluatorType;
    private String studentComments;
    private String supervisorComments;
}