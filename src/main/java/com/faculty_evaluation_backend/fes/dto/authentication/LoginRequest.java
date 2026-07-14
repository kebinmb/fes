package com.faculty_evaluation_backend.fes.dto.authentication;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username or email is required.")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required.")
    private String password;
}
