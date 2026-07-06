package com.faculty_evaluation_backend.fes.dto.faculty;

import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.FacultyLoadStatus;
import com.faculty_evaluation_backend.fes.entities.primary.enums.FacultyWorkloadSource;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class FacultyWorkloadResponse {
    private Long facultyWorkloadId;
    private String facultyId;
    private String facultyName;
    private College college;
    private Double loadLimit;
    private Integer schoolYear;
    private String semester;
    private String classCode;
    private String courseCode;
    private String programCode;
    private String yearLevel;
    private String sectionCode;
    private BigDecimal totalHoursPerWeek;
    private BigDecimal totalTeachingLoad;
    private Integer numberOfPreparations;
    private BigDecimal designationEtu;
    private BigDecimal totalWorkload;
    private BigDecimal overloadHours;
    private FacultyLoadStatus loadStatus;
    private FacultyWorkloadSource source;
    private String remarks;
}
