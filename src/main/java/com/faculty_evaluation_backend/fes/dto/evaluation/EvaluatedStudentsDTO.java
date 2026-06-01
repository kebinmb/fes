package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluatedStudentsDTO {
    private String evaluationSubmissionDate;
    private String evaluatorId;
    private String subjectCode;
    private String facultyId;
    private String firstname;
    private String lastname;

}
