package com.faculty_evaluation_backend.fes.dto.evaluation;

import com.faculty_evaluation_backend.fes.entities.primary.enums.EvaluationType;
import lombok.Data;

import java.util.Map;

@Data
public class BaseEvaluationDTO {
    private String facultyId;
    private String evaluatorId;
    private String classCode;
    private String subjectCode;
    private String semester;
    private String yearLevel;
    private Integer schoolYear;
    private String accessCode;

    private EvaluationType evaluationType;

    private Map<String, String> ratings;

    private String commentsOrFeedbacks;


}
