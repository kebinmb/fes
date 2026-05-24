package com.faculty_evaluation_backend.fes.dto.user_accounts;

import com.faculty_evaluation_backend.fes.entities.primary.enums.*;
import lombok.Data;

@Data
public class UpdateUserAccountDTO {

    private Long userId;

    private String username;

    private String email;

    private Role role;

    private College college;

    private Programs programs;

    private Majors majors;

    private Status status;

    private Boolean isEnabled;

    private Boolean isLocked;
}
