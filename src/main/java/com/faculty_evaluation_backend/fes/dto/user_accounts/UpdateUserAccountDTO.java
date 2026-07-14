package com.faculty_evaluation_backend.fes.dto.user_accounts;

import com.faculty_evaluation_backend.fes.entities.primary.enums.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserAccountDTO {

    @NotNull(message = "User ID is required.")
    private Long userId;

    @Size(max = 100, message = "Username must not exceed 100 characters.")
    private String username;

    @Email(message = "Email must be valid.")
    @Size(max = 150, message = "Email must not exceed 150 characters.")
    private String email;

    private Role role;

    private College college;

    private Programs programs;

    private Majors majors;

    private Status status;

    private Boolean isEnabled;

    private Boolean isLocked;
}
