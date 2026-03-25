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
    private String scheduleDay;
    private String scheduleTime;
    private String room;
    private String semester;
    private Integer schoolYear;

    // PrimaryStudentLoad
    private Long primaryStudentLoadId;
    private Integer loadId;
    private String studentId;
    private String yearLevel;
    private String grade;
}
