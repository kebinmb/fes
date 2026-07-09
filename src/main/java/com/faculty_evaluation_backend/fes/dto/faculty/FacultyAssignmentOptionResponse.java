package com.faculty_evaluation_backend.fes.dto.faculty;

public record FacultyAssignmentOptionResponse(
        String facultyId,
        String facultyName,
        String position,
        String college,
        String legacyDatabase
) {
}
