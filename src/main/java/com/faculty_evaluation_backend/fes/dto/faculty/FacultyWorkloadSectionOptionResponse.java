package com.faculty_evaluation_backend.fes.dto.faculty;

public record FacultyWorkloadSectionOptionResponse(
        Integer sectionId,
        String programCode,
        String yearLevel,
        String sectionCode
) {
}
