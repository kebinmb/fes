package com.faculty_evaluation_backend.fes.dto.subject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrimarySubjectResponse {
    private Long primarySubjectId;
    private String subjectCode;
    private String descriptiveTitle;
    private Double lecUnits;
    private Double labUnits;
    private Double lecHours;
    private Double labHours;
    private Double totalUnits;
    private Double totalHours;
    private String subjectType;
    private String sourceCampus;
    private String legacyDatabase;
}
