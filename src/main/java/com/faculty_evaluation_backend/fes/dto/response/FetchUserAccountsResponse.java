package com.faculty_evaluation_backend.fes.dto.response;

import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Role;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FetchUserAccountsResponse {
    private Long userId;
    private String username;
    private String email;
    private Role role;
    private Boolean isEnabled;
    private Boolean isLocked;
    private Boolean lastLoginAt;
    private College college;
    private Status status;
}
