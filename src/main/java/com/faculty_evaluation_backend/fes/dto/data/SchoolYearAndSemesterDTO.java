package com.faculty_evaluation_backend.fes.dto.data;

import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchoolYearAndSemesterDTO {

    private Long id;

    private Integer schoolYear;

    private Semester semester;

    private Status status;

    private Instant createdAt;
}