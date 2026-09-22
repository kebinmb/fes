package com.faculty_evaluation_backend.fes.dto.dashboard;

import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FacultyWorkloadCoverageFacultyResponse(
        String facultyId,
        String facultyName,
        String position,
        College college,
        Status status,
        Double loadLimit,
        boolean hasWorkload,
        Long workloadCount,
        BigDecimal totalHoursPerWeek,
        Integer numberOfPreparations
) {
    public boolean isHasWorkload() {
        return hasWorkload;
    }
}
