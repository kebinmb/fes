package com.faculty_evaluation_backend.fes.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StudentFacultyEvaluationDTO {
    private String studentId;
    private String studentFirstname;
    private String studentLastname;
    private String classCode;
    private String programCode;
    private String sectionCode;
    private String facultyId;
    private String facultyFirstname;
    private String facultyLastname;
    private LocalDateTime createdAt;
}
