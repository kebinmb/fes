package com.faculty_evaluation_backend.fes.dto.faculty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacultyLoadDTO {

    private String facultyId;

    private String firstname;
    private String lastname;
    private String middlename;

    private String position;

    private String subjectCode;


    private String programYearSection;

    private String campus;

    private Double loadLimit;

    private String typeOfLoad;
}