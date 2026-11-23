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
        BigDecimal totalTeachingLoad,
        String subjectType
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
                BigDecimal.ZERO,
                null
        );
    }

    public FacultyWorkloadClassOptionResponse(
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
        this(
                classCode,
                courseCode,
                sectionId,
                programCode,
                yearLevel,
                sectionCode,
                lecUnits,
                labUnits,
                lecHours,
                labHours,
                totalHoursPerWeek,
                totalTeachingLoad,
                null
        );
    }
}
