package com.faculty_evaluation_backend.fes.dto.faculty;

public record ClassFacultyReassignmentResponse(
        ClassFacultyAssignmentResponse assignment,
        String previousFacultyId,
        String newFacultyId,
        boolean changed
) {
}
