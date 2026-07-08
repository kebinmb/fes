package com.faculty_evaluation_backend.fes.entities.evaluation;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "faculty_evaluation_report",
        indexes = {
                @Index(
                        name = "idx_faculty_evaluation_report_faculty_term",
                        columnList = "faculty_id, school_year, semester"
                ),
                @Index(
                        name = "idx_faculty_evaluation_report_hash",
                        columnList = "report_hash"
                )
        }
)
public class FacultyEvaluationReport extends Auditable {

    @Id
    @Column(name = "report_id", length = 36)
    private String reportId;

    @Column(name = "faculty_id", nullable = false, length = 60)
    private String facultyId;

    @Column(name = "faculty_name")
    private String facultyName;

    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    @Column(name = "semester", nullable = false, length = 30)
    private String semester;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FacultyEvaluationReportStatus status;

    @Column(name = "report_hash", nullable = false, length = 64, unique = true)
    private String reportHash;

    @Lob
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "LONGTEXT")
    private String snapshotJson;

    @Column(name = "verification_url", nullable = false, length = 1000)
    private String verificationUrl;

    @Column(name = "generated_by_user_id")
    private Long generatedByUserId;

    @Column(name = "generated_by_username")
    private String generatedByUsername;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;
}
