package com.faculty_evaluation_backend.fes.dto.dashboard;

import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FacultyWorkloadCoverageFacultyResponse {
    private String facultyId;
    private String facultyName;
    private String position;
    private College college;
    private Status status;
    private Double loadLimit;
    private boolean hasWorkload;
    private Long workloadCount;
    private BigDecimal totalHoursPerWeek;
    private Integer numberOfPreparations;
}
