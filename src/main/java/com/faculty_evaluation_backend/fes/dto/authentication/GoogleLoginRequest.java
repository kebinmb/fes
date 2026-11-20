package com.faculty_evaluation_backend.fes.dto.authentication;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "Google ID token is required")
        @JsonAlias({"credential", "token", "id_token"})
        String idToken
) {}
