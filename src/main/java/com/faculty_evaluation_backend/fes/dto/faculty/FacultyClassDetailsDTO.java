package com.faculty_evaluation_backend.fes.dto.faculty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultyClassDetailsDTO {
    private String classCode;
    private String sectionCode;
    private String programCode;
    private String yearLevel;
}
