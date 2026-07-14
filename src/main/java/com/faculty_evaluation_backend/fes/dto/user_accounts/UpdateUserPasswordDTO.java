package com.faculty_evaluation_backend.fes.dto.user_accounts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserPasswordDTO {

    @NotNull(message = "User ID is required.")
    private Long userId;

    @NotBlank(message = "New password is required.")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters.")
    private String newPassword;
}
