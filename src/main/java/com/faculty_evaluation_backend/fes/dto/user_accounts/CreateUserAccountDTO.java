package com.faculty_evaluation_backend.fes.dto.user_accounts;

import com.faculty_evaluation_backend.fes.entities.primary.enums.*;
import lombok.Data;

@Data
public class CreateUserAccountDTO {

    private String username;

    private String email;

    private String password;

    private Role role;

    private College college;

    private Programs programs;

    private Majors majors;

    private Status status;
}
