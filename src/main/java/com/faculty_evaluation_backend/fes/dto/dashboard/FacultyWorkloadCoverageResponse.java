package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class FacultyWorkloadCoverageResponse {
    private Integer schoolYear;
    private String semester;
    private Long totalActiveFaculty;
    private Long withWorkloadCount;
    private Long withoutWorkloadCount;
    private Double coverageRate;
    private List<FacultyWorkloadCoverageFacultyResponse> withWorkload;
    private List<FacultyWorkloadCoverageFacultyResponse> withoutWorkload;
}
