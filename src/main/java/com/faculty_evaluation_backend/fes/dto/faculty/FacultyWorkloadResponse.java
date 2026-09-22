package com.faculty_evaluation_backend.fes.dto.faculty;

import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.FacultyWorkloadSource;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FacultyWorkloadResponse(
        Long facultyWorkloadId,
        String facultyId,
        String facultyName,
        College college,
        Double loadLimit,
        Integer schoolYear,
        String semester,
        String classCode,
        String courseCode,
        String programCode,
        String yearLevel,
        String sectionCode,
        BigDecimal totalHoursPerWeek,
        BigDecimal totalTeachingLoad,
        Integer numberOfPreparations,
        BigDecimal designationEtu,
        BigDecimal totalWorkload,
        BigDecimal overloadHours,
        String loadStatus,
        FacultyWorkloadSource source,
        String remarks
) {
}
