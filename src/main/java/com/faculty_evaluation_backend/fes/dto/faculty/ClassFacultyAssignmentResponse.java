package com.faculty_evaluation_backend.fes.dto.faculty;

public record ClassFacultyAssignmentResponse(
        Long primaryClassId,
        String classCode,
        String subjectCode,
        String subjectTitle,
        Integer sectionId,
        String programCode,
        String yearLevel,
        String sectionCode,
        String facultyId,
        String facultyName,
        Integer schoolYear,
        String semester,
        String legacyDatabase,
        String sourceCampus
) {
}
