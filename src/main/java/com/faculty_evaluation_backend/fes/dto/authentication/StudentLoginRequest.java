package com.faculty_evaluation_backend.fes.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentLoginRequest {
    @NotBlank(message = "Student ID is required.")
    private String studentId;

    @NotBlank(message = "Access code is required.")
    private String accessCode;
}
