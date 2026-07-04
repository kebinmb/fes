package com.faculty_evaluation_backend.fes.dto.faculty;

import com.faculty_evaluation_backend.fes.entities.primary.enums.FacultyWorkloadSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FacultyWorkloadRequest {

    private Long facultyWorkloadId;

    @NotBlank
    private String facultyId;

    @NotNull
    private Integer schoolYear;

    @NotBlank
    private String semester;

    @NotBlank
    private String courseCode;

    @NotBlank
    private String programCode;

    @NotBlank
    private String yearLevel;

    @NotBlank
    private String sectionCode;

    private BigDecimal totalTeachingLoad;

    private Integer numberOfPreparations;

    private BigDecimal designationEtu;

    private BigDecimal totalWorkload;

    private BigDecimal overloadHours;

    private FacultyWorkloadSource source;

    private String remarks;
}
