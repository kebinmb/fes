package com.faculty_evaluation_backend.fes.dto.user_accounts;

import com.faculty_evaluation_backend.fes.entities.primary.enums.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserAccountDTO {

    @NotBlank(message = "Username is required.")
    @Size(max = 100, message = "Username must not exceed 100 characters.")
    private String username;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be valid.")
    @Size(max = 150, message = "Email must not exceed 150 characters.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters.")
    private String password;

    @NotNull(message = "Role is required.")
    private Role role;

    @NotNull(message = "College is required.")
    private College college;

    private Programs programs;

    private Majors majors;

    @Size(max = 100, message = "Data source must not exceed 100 characters.")
    private String dataSource;

    @NotNull(message = "Status is required.")
    private Status status;
}
