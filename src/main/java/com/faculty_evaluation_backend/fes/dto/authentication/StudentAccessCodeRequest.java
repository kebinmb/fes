package com.faculty_evaluation_backend.fes.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudentAccessCodeRequest {
    @NotBlank(message = "Student ID is required.")
    private String studentId;

    @NotBlank(message = "Password is required.")
    private String password;
}
