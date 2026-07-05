package com.faculty_evaluation_backend.fes.dto.faculty;

public record FacultyWorkloadClassOptionResponse(
        String classCode,
        String courseCode,
        Integer sectionId,
        String programCode,
        String yearLevel,
        String sectionCode
) {
}
