package com.faculty_evaluation_backend.fes.dto.student;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentClassLoadDTO {

    // PrimaryClass
    private String classCode;
    private String facultyId;
    private String subjectCode;
    private Integer sectionId;
    private String semester;
    private Integer schoolYear;

    // PrimaryStudentLoad
    private String studentId;
    private String yearLevel;

    // Faculty
    private String college;

    // Others
    private String facultyName;
    private String subjectDescription;
}