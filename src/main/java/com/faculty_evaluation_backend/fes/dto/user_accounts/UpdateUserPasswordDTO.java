package com.faculty_evaluation_backend.fes.dto.user_accounts;

import lombok.Data;

@Data
public class UpdateUserPasswordDTO {

    private Long userId;

    private String newPassword;
}
