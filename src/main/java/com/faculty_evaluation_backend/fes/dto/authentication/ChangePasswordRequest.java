package com.faculty_evaluation_backend.fes.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmNewPassword;
}
