package com.faculty_evaluation_backend.fes.dto.evidence;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationEvidence;
import lombok.Builder;

import java.time.Instant;

@Builder
public record FacultyEvaluationEvidenceResponse(
        Long evidenceId,
        String facultyId,
        String classCode,
        String subjectCode,
        String yearLevel,
        String semester,
        Integer schoolYear,
        String criterion,
        String criterionCategory,
        String criterionLabel,
        String description,
        String uploadedBy,
        String originalFilename,
        String contentType,
        Long fileSize,
        Instant createdAt
) {
    public static FacultyEvaluationEvidenceResponse from(
            FacultyEvaluationEvidence evidence
    ) {
        return FacultyEvaluationEvidenceResponse.builder()
                .evidenceId(evidence.getFacultyEvaluationEvidenceId())
                .facultyId(evidence.getFacultyId())
                .classCode(evidence.getClassCode())
                .subjectCode(evidence.getSubjectCode())
                .yearLevel(evidence.getYearLevel())
                .semester(evidence.getSemester())
                .schoolYear(evidence.getSchoolYear())
                .criterion(evidence.getCriterion().name())
                .criterionCategory(evidence.getCriterion().getCategory())
                .criterionLabel(evidence.getCriterion().getLabel())
                .description(evidence.getDescription())
                .uploadedBy(evidence.getUploadedBy())
                .originalFilename(evidence.getOriginalFilename())
                .contentType(evidence.getContentType())
                .fileSize(evidence.getFileSize())
                .createdAt(evidence.getCreatedAt())
                .build();
    }
}
