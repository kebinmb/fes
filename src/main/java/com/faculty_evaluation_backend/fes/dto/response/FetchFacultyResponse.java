package com.faculty_evaluation_backend.fes.dto.response;

import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FetchFacultyResponse {
    private String facultyId;
    private String firstname;
    private String lastname;
    private String middlename;
    private String position;
    private String loadLimit;
    private Status status;
    private College college;
}
