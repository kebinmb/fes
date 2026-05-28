package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentEvaluationStatusResponse {
    private String studentId;
    private String programCode;
    private String yearLevel;
    private String sectionCode;
    private String subjectCode;
    private String createdAt;
    private String evaluationStatus;
}
