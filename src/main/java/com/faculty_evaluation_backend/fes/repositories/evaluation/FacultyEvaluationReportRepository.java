package com.faculty_evaluation_backend.fes.repositories.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationReport;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationReportPrintTrackingProjection;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyEvaluationReportRepository
        extends JpaRepository<FacultyEvaluationReport, String> {

    long countByFacultyIdAndSchoolYearAndSemester(
            String facultyId,
            Integer schoolYear,
            String semester
    );

    List<FacultyEvaluationReport>
    findByFacultyIdAndSchoolYearAndSemesterAndStatus(
            String facultyId,
            Integer schoolYear,
            String semester,
            FacultyEvaluationReportStatus status
    );

    @Query(
            value = """
                    SELECT
                        r.report_id AS reportId,
                        r.faculty_id AS facultyId,
                        r.faculty_name AS facultyName,
                        r.school_year AS schoolYear,
                        r.semester AS semester,
                        r.version_number AS versionNumber,
                        r.status AS status,
                        r.generated_by_username AS generatedByUsername,
                        r.generated_at AS generatedAt,
                        r.print_tracking_available AS printTrackingAvailable,
                        r.printed_at AS printedAt,
                        r.printed_by_username AS printedByUsername,
                        r.print_count AS printCount,
                        r.annex_d_printed_at AS annexDPrintedAt,
                        r.annex_d_printed_by_username AS annexDPrintedByUsername,
                        r.annex_d_print_count AS annexDPrintCount
                    FROM faculty_evaluation_report r
                    WHERE (:schoolYear IS NULL OR r.school_year = :schoolYear)
                      AND (
                            :semester IS NULL
                            OR LOWER(TRIM(r.semester)) = LOWER(TRIM(CONVERT(:semester USING utf8mb4) COLLATE utf8mb4_unicode_ci))
                      )
                      AND (
                            :status IS NULL
                            OR r.status = :status
                      )
                      AND (
                            :printStatus IS NULL
                            OR :printStatus = 'ALL'
                            OR (:printStatus = 'REPORT_PRINTED' AND r.printed_at IS NOT NULL)
                            OR (:printStatus = 'REPORT_NOT_PRINTED' AND r.printed_at IS NULL)
                            OR (:printStatus = 'ANNEX_D_PRINTED' AND r.annex_d_printed_at IS NOT NULL)
                            OR (:printStatus = 'ANNEX_D_NOT_PRINTED' AND r.annex_d_printed_at IS NULL)
                      )
                      AND (
                            :search IS NULL
                            OR LOWER(COALESCE(r.report_id, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.faculty_id, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.faculty_name, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.generated_by_username, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.printed_by_username, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.annex_d_printed_by_username, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                      )
                    ORDER BY r.generated_at DESC, r.report_id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM faculty_evaluation_report r
                    WHERE (:schoolYear IS NULL OR r.school_year = :schoolYear)
                      AND (
                            :semester IS NULL
                            OR LOWER(TRIM(r.semester)) = LOWER(TRIM(CONVERT(:semester USING utf8mb4) COLLATE utf8mb4_unicode_ci))
                      )
                      AND (
                            :status IS NULL
                            OR r.status = :status
                      )
                      AND (
                            :printStatus IS NULL
                            OR :printStatus = 'ALL'
                            OR (:printStatus = 'REPORT_PRINTED' AND r.printed_at IS NOT NULL)
                            OR (:printStatus = 'REPORT_NOT_PRINTED' AND r.printed_at IS NULL)
                            OR (:printStatus = 'ANNEX_D_PRINTED' AND r.annex_d_printed_at IS NOT NULL)
                            OR (:printStatus = 'ANNEX_D_NOT_PRINTED' AND r.annex_d_printed_at IS NULL)
                      )
                      AND (
                            :search IS NULL
                            OR LOWER(COALESCE(r.report_id, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.faculty_id, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.faculty_name, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.generated_by_username, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.printed_by_username, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                            OR LOWER(COALESCE(r.annex_d_printed_by_username, '')) LIKE LOWER(CONCAT('%', CONVERT(:search USING utf8mb4) COLLATE utf8mb4_unicode_ci, '%'))
                      )
                    """,
            nativeQuery = true
    )
    Page<FacultyEvaluationReportPrintTrackingProjection> findPrintTrackingReports(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            @Param("search") String search,
            @Param("status") String status,
            @Param("printStatus") String printStatus,
            Pageable pageable
    );
}
