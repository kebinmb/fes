package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentEvaluationStatusDTO {

    private String studentId;

    private String programCode;

    private String yearLevel;

    private String sectionCode;

    private String evaluationStatus;
}