package com.faculty_evaluation_backend.fes.dto.faculty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FacultyDTO {
    private String facultyId;
    private String lastname;
    private String firstname;
    private String position;
    private Double loadLimit;
    private String middlename;
    private String college;
    private String status;
}
