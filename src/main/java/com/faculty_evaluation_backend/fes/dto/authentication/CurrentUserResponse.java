package com.faculty_evaluation_backend.fes.dto.authentication;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CurrentUserResponse(
        boolean authenticated,
        String studentId,
        String userId,
        String administratorId,
        String evaluatorId,
        String role,
        String college,
        String program,
        Boolean requiresPasswordChange
) {
}
