package com.faculty_evaluation_backend.fes.dto.data;

import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolYearAndSemesterDTO {
    private Integer schoolYear;
    private String semester;
    private Status status;
}
