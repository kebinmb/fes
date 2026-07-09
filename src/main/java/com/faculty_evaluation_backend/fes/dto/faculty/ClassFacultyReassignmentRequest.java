package com.faculty_evaluation_backend.fes.dto.faculty;

import jakarta.validation.constraints.NotBlank;

public record ClassFacultyReassignmentRequest(
        @NotBlank(message = "Faculty ID is required.")
        String facultyId,
        String expectedCurrentFacultyId
) {
}
