package com.faculty_evaluation_backend.fes.dto.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpsertPrimarySubjectRequest {
    @NotBlank(message = "Subject code is required")
    private String subjectCode;

    @NotBlank(message = "Descriptive title is required")
    private String descriptiveTitle;

    @NotNull(message = "Lecture units cannot be null")
    @PositiveOrZero(message = "Lecture units must be zero or positive")
    private Double lecUnits;

    @NotNull(message = "Lab units cannot be null")
    @PositiveOrZero(message = "Lab units must be zero or positive")
    private Double labUnits;

    @NotNull(message = "Lecture hours cannot be null")
    @PositiveOrZero(message = "Lecture hours must be zero or positive")
    private Double lecHours;

    @NotNull(message = "Lab hours cannot be null")
    @PositiveOrZero(message = "Lab hours must be zero or positive")
    private Double labHours;

    private String subjectType;
}
