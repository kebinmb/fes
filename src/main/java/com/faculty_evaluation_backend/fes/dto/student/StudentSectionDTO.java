package com.faculty_evaluation_backend.fes.dto.student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSectionDTO {
    private String programCode;

    private String yearLevel;

    private String sectionCode;

    private Long totalStudents;

    private Long evaluatedStudents;

    private Long notYetEvaluated;
}
