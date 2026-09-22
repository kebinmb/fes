package com.faculty_evaluation_backend.fes.dto.response;

import com.faculty_evaluation_backend.fes.entities.primary.enums.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class FetchUserAccountsResponse {

    private Long userId;

    private String username;

    private String email;

    private Role role;

    private Boolean isEnabled;

    private Boolean isLocked;

    private Instant lastLoginAt;

    private String dataSource;

    private College college;

    private Programs programs;

    private Majors majors;

    private Status status;
}
