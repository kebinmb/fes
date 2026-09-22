package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

import java.util.List;

@Builder
public record FacultyWorkloadCoverageResponse(
        Integer schoolYear,
        String semester,
        Long totalActiveFaculty,
        Long withWorkloadCount,
        Long withoutWorkloadCount,
        Double coverageRate,
        List<FacultyWorkloadCoverageFacultyResponse> withWorkload,
        List<FacultyWorkloadCoverageFacultyResponse> withoutWorkload
) {
}
