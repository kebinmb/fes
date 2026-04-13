package com.faculty_evaluation_backend.fes.dto.faculty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacultyClassDTO {
    private String subjectCode;
    private String facultyId;
    private Integer schoolYear;
    private String semester;
    private String classCode;
    private String yearLevel;
}
