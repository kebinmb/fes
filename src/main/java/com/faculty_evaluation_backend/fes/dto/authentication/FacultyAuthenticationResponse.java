package com.faculty_evaluation_backend.fes.dto.authentication;

import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacultyAuthenticationResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn; // in seconds
    private String evaluatorId;
    private College college;
}
