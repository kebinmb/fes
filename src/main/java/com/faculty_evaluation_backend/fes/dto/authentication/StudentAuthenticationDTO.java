package com.faculty_evaluation_backend.fes.dto.authentication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAuthenticationDTO {
    private String studentId;
    private String password;
    private String email;
    private String sourceTable;
}
