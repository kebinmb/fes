package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.dto.faculty.FacultyWorkloadSectionOptionResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentSectionDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimarySection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrimarySectionRepository extends JpaRepository<PrimarySection, Long> {

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM PrimarySection s " +
           "WHERE s.sectionId = :sectionId " +
           "AND s.programCode = :programCode " +
           "AND (s.sectionCode = :sectionCode OR (s.sectionCode IS NULL AND :sectionCode IS NULL)) " +
           "AND (s.yearLevel = :yearLevel OR (s.yearLevel IS NULL AND :yearLevel IS NULL))")
    boolean existsBySectionIdAndProgramCodeAndSectionCodeAndYearLevel(
            @Param("sectionId") Integer sectionId,
            @Param("programCode") String programCode,
            @Param("sectionCode") String sectionCode,
            @Param("yearLevel") String yearLevel
    );

    boolean existsByLegacyDatabaseAndLegacyId(
            String legacyDatabase,
            String legacyId
    );

    @Query("""
                SELECT s.legacyId
                FROM PrimarySection s
                WHERE s.legacyDatabase = :database
            """)
    List<String> findLegacyIdsByDatabase(@Param("database") String database);

    @Query(
            value = """
                    SELECT
                        ps.program_code AS programCode,
                        ps.year_level AS yearLevel,
                        ps.section_code AS sectionCode,
                        COUNT(DISTINCT psl.student_id) AS totalStudents,
                        COUNT(DISTINCT fes.evaluator_id) AS evaluatedStudents,
                        (
                            COUNT(DISTINCT psl.student_id)
                            - COUNT(DISTINCT fes.evaluator_id)
                        ) AS notYetEvaluated
                    FROM primary_student_load psl
                    INNER JOIN primary_class pc
                        ON (CAST(psl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)
                    INNER JOIN primary_section ps
                        ON pc.section_id = ps.section_id
                    LEFT JOIN faculty_workload fw_overload
                        ON fw_overload.faculty_id = pc.faculty_id
                       AND fw_overload.school_year = pc.school_year
                       AND (
                            LOWER(TRIM(fw_overload.semester)) = LOWER(TRIM(pc.semester))
                            OR (
                                UPPER(TRIM(fw_overload.semester)) = 'FIRST_SEMESTER'
                                AND LOWER(TRIM(pc.semester)) = '1st'
                            )
                            OR (
                                UPPER(TRIM(fw_overload.semester)) = 'SECOND_SEMESTER'
                                AND LOWER(TRIM(pc.semester)) = '2nd'
                            )
                            OR (
                                UPPER(TRIM(fw_overload.semester)) = 'SUMMER_SEMESTER'
                                AND LOWER(TRIM(pc.semester)) = 'summer'
                            )
                       )
                       AND (
                            fw_overload.class_code = pc.class_code
                            OR (
                                (fw_overload.class_code IS NULL OR fw_overload.class_code = '')
                                AND fw_overload.course_code = pc.subject_code
                                AND fw_overload.program_code = ps.program_code
                                AND fw_overload.year_level = ps.year_level
                                AND fw_overload.section_code = ps.section_code
                            )
                       )
                       AND LOWER(TRIM(fw_overload.load_status)) = 'overload'
                    LEFT JOIN faculty_evaluation_score fes
                        ON (CAST(psl.student_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(fes.evaluator_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                       AND (CAST(psl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(fes.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)
                       AND fes.school_year = pc.school_year
                       AND (CAST(fes.semester AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pc.semester AS CHAR) COLLATE utf8mb4_unicode_ci)
                    WHERE pc.school_year = :schoolYear
                      AND (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                          (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                      AND fw_overload.faculty_workload_id IS NULL
                      AND (
                            :programCode IS NULL
                            OR (CAST(ps.program_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                               (CAST(:programCode AS CHAR) COLLATE utf8mb4_unicode_ci)
                      )
                      AND (
                            :yearLevel IS NULL
                            OR (CAST(ps.year_level AS CHAR) COLLATE utf8mb4_unicode_ci) =
                               (CAST(:yearLevel AS CHAR) COLLATE utf8mb4_unicode_ci)
                      )
                      AND (
                            :sectionCode IS NULL
                            OR (CAST(ps.section_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                               (CAST(:sectionCode AS CHAR) COLLATE utf8mb4_unicode_ci)
                      )
                    GROUP BY
                        ps.program_code,
                        ps.year_level,
                        ps.section_code
                    ORDER BY
                        ps.program_code ASC,
                        ps.year_level ASC,
                        ps.section_code ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT
                            ps.program_code,
                            ps.year_level,
                            ps.section_code
                        FROM primary_student_load psl
                        INNER JOIN primary_class pc
                            ON (CAST(psl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                               (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)
                        INNER JOIN primary_section ps
                            ON pc.section_id = ps.section_id
                        LEFT JOIN faculty_workload fw_overload
                            ON fw_overload.faculty_id = pc.faculty_id
                           AND fw_overload.school_year = pc.school_year
                           AND (
                                LOWER(TRIM(fw_overload.semester)) = LOWER(TRIM(pc.semester))
                                OR (
                                    UPPER(TRIM(fw_overload.semester)) = 'FIRST_SEMESTER'
                                    AND LOWER(TRIM(pc.semester)) = '1st'
                                )
                                OR (
                                    UPPER(TRIM(fw_overload.semester)) = 'SECOND_SEMESTER'
                                    AND LOWER(TRIM(pc.semester)) = '2nd'
                                )
                                OR (
                                    UPPER(TRIM(fw_overload.semester)) = 'SUMMER_SEMESTER'
                                    AND LOWER(TRIM(pc.semester)) = 'summer'
                                )
                           )
                           AND (
                                fw_overload.class_code = pc.class_code
                                OR (
                                    (fw_overload.class_code IS NULL OR fw_overload.class_code = '')
                                    AND fw_overload.course_code = pc.subject_code
                                    AND fw_overload.program_code = ps.program_code
                                    AND fw_overload.year_level = ps.year_level
                                    AND fw_overload.section_code = ps.section_code
                                )
                           )
                           AND LOWER(TRIM(fw_overload.load_status)) = 'overload'
                        WHERE pc.school_year = :schoolYear
                          AND (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                              (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                          AND fw_overload.faculty_workload_id IS NULL
                          AND (
                                :programCode IS NULL
                                OR (CAST(ps.program_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                                   (CAST(:programCode AS CHAR) COLLATE utf8mb4_unicode_ci)
                          )
                          AND (
                                :yearLevel IS NULL
                                OR (CAST(ps.year_level AS CHAR) COLLATE utf8mb4_unicode_ci) =
                                   (CAST(:yearLevel AS CHAR) COLLATE utf8mb4_unicode_ci)
                          )
                          AND (
                                :sectionCode IS NULL
                                OR (CAST(ps.section_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                                   (CAST(:sectionCode AS CHAR) COLLATE utf8mb4_unicode_ci)
                          )
                        GROUP BY
                            ps.program_code,
                            ps.year_level,
                            ps.section_code
                    ) grouped_sections
                    """,
            nativeQuery = true
    )
    Page<Object[]> getStudentSectionEvaluationData(
            @Param("programCode") String programCode,
            @Param("yearLevel") String yearLevel,
            @Param("sectionCode") String sectionCode,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            Pageable pageable
    );

    @Query("""
            SELECT DISTINCT new com.faculty_evaluation_backend.fes.dto.faculty.FacultyWorkloadSectionOptionResponse(
                ps.sectionId,
                ps.programCode,
                ps.yearLevel,
                ps.sectionCode
            )
            FROM PrimaryClass pc
            INNER JOIN pc.section ps
            WHERE pc.schoolYear = :schoolYear
              AND pc.semester = :semester
              AND pc.sectionId IS NOT NULL
            ORDER BY
                ps.programCode ASC,
                ps.yearLevel ASC,
                ps.sectionCode ASC
            """)
    List<FacultyWorkloadSectionOptionResponse> findAvailableFacultyWorkloadSections(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );


}
