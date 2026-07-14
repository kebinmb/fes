package com.faculty_evaluation_backend.fes.repositories.evaluation;


import com.faculty_evaluation_backend.fes.dto.evaluation.EvaluatedStudentsDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentFacultyEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDetailsDTO;
import com.faculty_evaluation_backend.fes.dto.dashboard.SupervisorEvaluationDashboardProjection;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacultyEvaluationScoreRepository extends JpaRepository<FacultyEvaluationScore, Long> {

    @Query("""
                SELECT f FROM FacultyEvaluationScore f
                JOIN FETCH f.faculty
            """)
    Page<FacultyEvaluationScore> findAllWithFaculty(Pageable pageable);

    boolean existsByFacultyIdAndEvaluatorIdAndClassCodeAndSubjectCodeAndYearLevelAndSemesterAndSchoolYear(String facultyId, String evaluatorId, String classCode, String subjectCode, String yearLevel, String semester, Integer schoolYear);

    Integer countDistinctEvaluatedSubjectsByEvaluatorId(String evaluatorId);

    List<FacultyEvaluationScore> findByFacultyId(String facultyId);

    List<FacultyEvaluationScore> findByFacultyIdAndClassCode(String facultyId, String classCode);

    @Query("""
                SELECT fes
                FROM FacultyEvaluationScore fes
                JOIN FETCH fes.faculty
                WHERE fes.facultyId = :facultyId
                  AND fes.schoolYear = :schoolYear
                  AND fes.semester = :semester
            """)
    List<FacultyEvaluationScore> findByFacultyIdAndSchoolYearAndSemesterWithFaculty(
            @Param("facultyId") String facultyId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(
            value = """
                    SELECT DISTINCT
                        fes.class_code,
                        ps.section_code,
                        ps.program_code,
                        ps.year_level
                    FROM faculty_evaluation_score fes
                    INNER JOIN primary_class pc
                        ON (CAST(fes.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)
                    INNER JOIN primary_section ps
                        ON pc.section_id = ps.section_id
                    WHERE (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                          (CAST(:facultyId AS CHAR) COLLATE utf8mb4_unicode_ci)
                      AND pc.school_year = :schoolYear
                      AND (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                          (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                    """,
            nativeQuery = true
    )
    List<Object[]> findDistinctClassDetailsByFacultyIdAndSchoolYearAndSemester(
            @Param("facultyId") String facultyId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    List<FacultyEvaluationScore> findByFacultyIdAndClassCodeAndSchoolYearAndSemester(String facultyId, String classCode, Integer schoolYear, String semester);

    @Query(value = """
            SELECT
                ps.student_id,
                ps.student_firstname,
                ps.student_lastname,
                pc.class_code,
                pse.program_code,
                pse.section_code,
                pc.faculty_id,
                pf.firstname,
                pf.lastname,
                fes.created_at
            FROM faculty_evaluation_score fes
            INNER JOIN primary_student ps
                ON (CAST(fes.evaluator_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(ps.student_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            INNER JOIN primary_student_load psl
                ON (CAST(ps.student_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(psl.student_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            INNER JOIN primary_class pc
                ON (CAST(psl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)
            INNER JOIN primary_section pse
                ON pc.section_id = pse.section_id
            INNER JOIN primary_faculty pf
                ON (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            WHERE
                (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(ps.student_id) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(ps.student_firstname) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(ps.student_lastname) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pc.class_code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pse.program_code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pse.section_code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pc.faculty_id) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pf.firstname) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pf.lastname) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            """, countQuery = """
            SELECT COUNT(*)
            FROM faculty_evaluation_score fes
            INNER JOIN primary_student ps
                ON (CAST(fes.evaluator_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(ps.student_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            INNER JOIN primary_student_load psl
                ON (CAST(ps.student_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(psl.student_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            INNER JOIN primary_class pc
                ON (CAST(psl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)
            INNER JOIN primary_section pse
                ON pc.section_id = pse.section_id
            INNER JOIN primary_faculty pf
                ON (CAST(pc.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                   (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
            WHERE
                (
                    :search IS NULL
                    OR :search = ''
                    OR LOWER(ps.student_id) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(ps.student_firstname) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(ps.student_lastname) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pc.class_code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pse.program_code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pse.section_code) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pc.faculty_id) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pf.firstname) LIKE LOWER(CONCAT('%', :search, '%'))
                    OR LOWER(pf.lastname) LIKE LOWER(CONCAT('%', :search, '%'))
                )
            """, nativeQuery = true)
    Page<Object[]> findStudentFacultyEvaluationDetails(@Param("search") String search, Pageable pageable);

    @Query(
            value = """
                    SELECT
                        DATE_FORMAT(fes.created_at, '%Y-%m-%d %H:%i:%s') AS evaluationSubmissionDate,
                        fes.evaluator_id AS evaluatorId,
                        fes.subject_code AS subjectCode,
                        fes.faculty_id AS facultyId,
                        ps.student_lastname AS studentLastname,
                        ps.student_firstname AS studentFirstname,
                        pf.firstname AS firstname,
                        pf.lastname AS lastname
                    FROM faculty_evaluation_score fes
                    INNER JOIN primary_faculty pf
                        ON (CAST(fes.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    INNER JOIN primary_student ps
                        ON (CAST(fes.evaluator_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(ps.student_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    INNER JOIN user_accounts ua
                        ON ua.user_id = :userId
                       AND (CAST(ua.data_source AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(ps.legacy_database AS CHAR) COLLATE utf8mb4_unicode_ci)
                    WHERE (
                        :searchTerm IS NULL
                        OR :searchTerm = ''
                        OR (LOWER(CAST(fes.evaluator_id AS CHAR)) COLLATE utf8mb4_unicode_ci)
                            LIKE (LOWER(CONCAT('%', CAST(:searchTerm AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                        OR (LOWER(CAST(ps.student_firstname AS CHAR)) COLLATE utf8mb4_unicode_ci)
                            LIKE (LOWER(CONCAT('%', CAST(:searchTerm AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                        OR (LOWER(CAST(ps.student_lastname AS CHAR)) COLLATE utf8mb4_unicode_ci)
                            LIKE (LOWER(CONCAT('%', CAST(:searchTerm AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                    )
                    ORDER BY fes.created_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM faculty_evaluation_score fes
                    INNER JOIN primary_faculty pf
                        ON (CAST(fes.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    INNER JOIN primary_student ps
                        ON (CAST(fes.evaluator_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(ps.student_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    INNER JOIN user_accounts ua
                        ON ua.user_id = :userId
                       AND (CAST(ua.data_source AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(ps.legacy_database AS CHAR) COLLATE utf8mb4_unicode_ci)
                    WHERE (
                        :searchTerm IS NULL
                        OR :searchTerm = ''
                        OR (LOWER(CAST(fes.evaluator_id AS CHAR)) COLLATE utf8mb4_unicode_ci)
                            LIKE (LOWER(CONCAT('%', CAST(:searchTerm AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                        OR (LOWER(CAST(ps.student_firstname AS CHAR)) COLLATE utf8mb4_unicode_ci)
                            LIKE (LOWER(CONCAT('%', CAST(:searchTerm AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                        OR (LOWER(CAST(ps.student_lastname AS CHAR)) COLLATE utf8mb4_unicode_ci)
                            LIKE (LOWER(CONCAT('%', CAST(:searchTerm AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                    )
                    """,
            nativeQuery = true
    )
    Page<Object[]> findEvaluatedStudents(
            @Param("userId") Long userId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT
                        pf.faculty_id AS facultyId,
                        TRIM(CONCAT(
                            COALESCE(pf.firstname, ''),
                            ' ',
                            COALESCE(pf.middlename, ''),
                            ' ',
                            COALESCE(pf.lastname, '')
                        )) AS facultyName,
                        pf.position AS position,
                        pf.college AS college,
                        pf.legacy_database AS legacyDatabase,
                        cs.campus AS campus,
                        cs.assigned_class_count AS assignedClassCount,
                        COALESCE(se.supervisor_evaluation_count, 0) AS supervisorEvaluationCount,
                        se.supervisor_ids AS supervisorIds,
                        se.supervisor_names AS supervisorNames,
                        se.supervisor_positions AS supervisorPositions,
                        se.supervisor_average_score AS supervisorAverageScore,
                        se.last_evaluated_at AS lastEvaluatedAt
                    FROM primary_faculty pf
                    INNER JOIN (
                        SELECT
                            pc.faculty_id,
                            COUNT(DISTINCT pc.class_code) AS assigned_class_count,
                            MAX(pc.source_campus) AS campus
                        FROM primary_class pc
                        WHERE pc.class_code IS NOT NULL
                          AND pc.school_year = :schoolYear
                          AND (
                                (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                                (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(CAST(pc.semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(CAST(pc.semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(CAST(pc.semester AS CHAR))) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY pc.faculty_id
                    ) cs
                        ON (CAST(cs.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    LEFT JOIN (
                        SELECT
                            fes.faculty_id,
                            COUNT(DISTINCT fes.faculty_evaluation_score_id) AS supervisor_evaluation_count,
                            GROUP_CONCAT(DISTINCT fes.evaluator_id ORDER BY fes.evaluator_id SEPARATOR ', ') AS supervisor_ids,
                            GROUP_CONCAT(
                                DISTINCT TRIM(CONCAT(
                                    COALESCE(sua.firstname, ''),
                                    ' ',
                                    COALESCE(sua.lastname, '')
                                ))
                                ORDER BY sua.lastname, sua.firstname
                                SEPARATOR ', '
                            ) AS supervisor_names,
                            GROUP_CONCAT(DISTINCT sua.user_role ORDER BY sua.user_role SEPARATOR ', ') AS supervisor_positions,
                            ROUND(AVG(fes.overall_average_score), 2) AS supervisor_average_score,
                            MAX(fes.created_at) AS last_evaluated_at
                        FROM faculty_evaluation_score fes
                        LEFT JOIN user_accounts sua
                            ON (CAST(sua.user_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                               (CAST(fes.evaluator_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                        WHERE fes.evaluation_type = 'ROLE_PROGRAM_CHAIR'
                          AND fes.school_year = :schoolYear
                          AND (
                                (LOWER(TRIM(CAST(fes.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                                (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(CAST(fes.semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(CAST(fes.semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(CAST(fes.semester AS CHAR))) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY fes.faculty_id
                    ) se
                        ON (CAST(se.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    WHERE UPPER(TRIM(CAST(pf.status AS CHAR))) = 'ACTIVE'
                      AND (
                            pf.college IS NULL
                            OR UPPER(TRIM(CAST(pf.college AS CHAR))) <> 'FOR_MIGRATION'
                      )
                      AND (
                            :legacyDatabase IS NULL
                            OR :legacyDatabase = ''
                            OR (UPPER(TRIM(CAST(pf.legacy_database AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                               (UPPER(TRIM(CAST(:legacyDatabase AS CHAR))) COLLATE utf8mb4_unicode_ci)
                      )
                      AND (
                            :campus IS NULL
                            OR :campus = ''
                            OR (UPPER(TRIM(CAST(cs.campus AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                               (UPPER(TRIM(CAST(:campus AS CHAR))) COLLATE utf8mb4_unicode_ci)
                      )
                      AND (
                            :evaluationStatus IS NULL
                            OR :evaluationStatus = ''
                            OR (
                                :evaluationStatus = 'EVALUATED'
                                AND COALESCE(se.supervisor_evaluation_count, 0) > 0
                            )
                            OR (
                                :evaluationStatus = 'PENDING'
                                AND COALESCE(se.supervisor_evaluation_count, 0) = 0
                            )
                      )
                      AND (
                            :search IS NULL
                            OR :search = ''
                            OR (LOWER(COALESCE(CAST(pf.faculty_id AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(pf.firstname AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(pf.lastname AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(pf.college AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(se.supervisor_names AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                      )
                    ORDER BY
                        CASE WHEN COALESCE(se.supervisor_evaluation_count, 0) = 0 THEN 0 ELSE 1 END ASC,
                        pf.lastname ASC,
                        pf.firstname ASC,
                        pf.faculty_id ASC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM primary_faculty pf
                    INNER JOIN (
                        SELECT pc.faculty_id, MAX(pc.source_campus) AS campus
                        FROM primary_class pc
                        WHERE pc.class_code IS NOT NULL
                          AND pc.school_year = :schoolYear
                          AND (
                                (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                                (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(CAST(pc.semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(CAST(pc.semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(CAST(pc.semester AS CHAR))) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY pc.faculty_id
                    ) cs
                        ON (CAST(cs.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    LEFT JOIN (
                        SELECT
                            fes.faculty_id,
                            COUNT(DISTINCT fes.faculty_evaluation_score_id) AS supervisor_evaluation_count,
                            GROUP_CONCAT(
                                DISTINCT TRIM(CONCAT(
                                    COALESCE(sua.firstname, ''),
                                    ' ',
                                    COALESCE(sua.lastname, '')
                                ))
                                SEPARATOR ', '
                            ) AS supervisor_names
                        FROM faculty_evaluation_score fes
                        LEFT JOIN user_accounts sua
                            ON (CAST(sua.user_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                               (CAST(fes.evaluator_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                        WHERE fes.evaluation_type = 'ROLE_PROGRAM_CHAIR'
                          AND fes.school_year = :schoolYear
                          AND (
                                (LOWER(TRIM(CAST(fes.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                                (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(CAST(fes.semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(CAST(fes.semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(CAST(:semester AS CHAR))) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(CAST(fes.semester AS CHAR))) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY fes.faculty_id
                    ) se
                        ON (CAST(se.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    WHERE UPPER(TRIM(CAST(pf.status AS CHAR))) = 'ACTIVE'
                      AND (
                            pf.college IS NULL
                            OR UPPER(TRIM(CAST(pf.college AS CHAR))) <> 'FOR_MIGRATION'
                      )
                      AND (
                            :legacyDatabase IS NULL
                            OR :legacyDatabase = ''
                            OR (UPPER(TRIM(CAST(pf.legacy_database AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                               (UPPER(TRIM(CAST(:legacyDatabase AS CHAR))) COLLATE utf8mb4_unicode_ci)
                      )
                      AND (
                            :campus IS NULL
                            OR :campus = ''
                            OR (UPPER(TRIM(CAST(cs.campus AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                               (UPPER(TRIM(CAST(:campus AS CHAR))) COLLATE utf8mb4_unicode_ci)
                      )
                      AND (
                            :evaluationStatus IS NULL
                            OR :evaluationStatus = ''
                            OR (
                                :evaluationStatus = 'EVALUATED'
                                AND COALESCE(se.supervisor_evaluation_count, 0) > 0
                            )
                            OR (
                                :evaluationStatus = 'PENDING'
                                AND COALESCE(se.supervisor_evaluation_count, 0) = 0
                            )
                      )
                      AND (
                            :search IS NULL
                            OR :search = ''
                            OR (LOWER(COALESCE(CAST(pf.faculty_id AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(pf.firstname AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(pf.lastname AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(pf.college AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(se.supervisor_names AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                      )
                    """,
            nativeQuery = true
    )
    Page<SupervisorEvaluationDashboardProjection> findSupervisorEvaluationDashboard(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            @Param("search") String search,
            @Param("evaluationStatus") String evaluationStatus,
            @Param("legacyDatabase") String legacyDatabase,
            @Param("campus") String campus,
            Pageable pageable
    );

}



