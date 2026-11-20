package com.faculty_evaluation_backend.fes.dto.faculty;

import java.math.BigDecimal;

public record FacultyWorkloadClassOptionResponse(
        String classCode,
        String courseCode,
        Integer sectionId,
        String programCode,
        String yearLevel,
        String sectionCode,
        Double lecUnits,
        Double labUnits,
        Double lecHours,
        Double labHours,
        BigDecimal totalHoursPerWeek,
        BigDecimal totalTeachingLoad
) {
    public FacultyWorkloadClassOptionResponse(
            String classCode,
            String courseCode,
            Integer sectionId,
            String programCode,
            String yearLevel,
            String sectionCode
    ) {
        this(
                classCode,
                courseCode,
                sectionId,
                programCode,
                yearLevel,
                sectionCode,
                0.0,
                0.0,
                0.0,
                0.0,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }
}
