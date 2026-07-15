package com.faculty_evaluation_backend.fes.entities.evaluation;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.EvaluationEvidenceCriterion;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.utilities.normalization.SemesterNormalizer;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "faculty_evaluation_evidence",
        indexes = {
                @Index(
                        name = "idx_evidence_faculty_context",
                        columnList = "faculty_id, school_year, semester"
                ),
                @Index(
                        name = "idx_evidence_faculty_created_at",
                        columnList = "faculty_id, created_at"
                ),
                @Index(
                        name = "idx_evidence_faculty_context_created",
                        columnList = "faculty_id, school_year, semester, created_at"
                ),
                @Index(
                        name = "idx_evidence_faculty_criterion_created",
                        columnList = "faculty_id, criterion, created_at"
                ),
                @Index(
                        name = "idx_evidence_faculty_subject_created",
                        columnList = "faculty_id, class_code, subject_code, created_at"
                ),
                @Index(
                        name = "idx_evidence_criterion",
                        columnList = "criterion"
                ),
                @Index(
                        name = "idx_evidence_uploaded_by",
                        columnList = "uploaded_by"
                )
        }
)
public class FacultyEvaluationEvidence extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faculty_evaluation_evidence_id")
    private Long facultyEvaluationEvidenceId;

    @Column(name = "faculty_id", nullable = false, length = 60)
    private String facultyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "faculty_id",
            referencedColumnName = "faculty_id",
            insertable = false,
            updatable = false
    )
    private PrimaryFaculty faculty;

    @Column(name = "class_code", length = 255)
    private String classCode;

    @Column(name = "subject_code", length = 255)
    private String subjectCode;

    @Column(name = "year_level", length = 50)
    private String yearLevel;

    @Column(name = "semester", length = 30)
    private String semester;

    @Column(name = "school_year")
    private Integer schoolYear;

    @Enumerated(EnumType.STRING)
    @Column(name = "criterion", nullable = false, length = 80)
    private EvaluationEvidenceCriterion criterion;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "uploaded_by", nullable = false, length = 100)
    private String uploadedBy;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "content_type", length = 150)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @PrePersist
    @PreUpdate
    void prepareForSave() {
        semester = SemesterNormalizer.toCanonicalValue(semester);
    }
}
