package com.faculty_evaluation_backend.fes.dto.faculty;

import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacultyProgramLoadsDTO {
    private String facultyId;
    private String firstname;
    private String lastname;
    private String status;
    private String position;
    private String college;
    private String campus;
}
