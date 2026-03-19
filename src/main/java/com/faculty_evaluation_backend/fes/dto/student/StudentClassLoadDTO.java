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
    // From primary_class
    private String classCode;
    private String facultyId;
    private String subjectCode;
    private Integer section;
    private String scheduleDay;
    private String scheduleTime;
    private String room;
    private String semester;
    private Integer schoolYear;

    // From primary_student_load
    private Long studentLoadId;
    private String studentId;
    private String grade;
}
