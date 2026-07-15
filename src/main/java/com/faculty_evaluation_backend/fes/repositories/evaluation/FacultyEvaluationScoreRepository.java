package com.faculty_evaluation_backend.fes.repositories.evaluation;


import com.faculty_evaluation_backend.fes.dto.evaluation.EvaluatedStudentsDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentFacultyEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDetailsDTO;
import com.faculty_evaluation_backend.fes.dto.dashboard.SupervisorEvaluationDashboardProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.SupervisorEvaluationDashboardMetricsProjection;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.primary.enums.EvaluationType;
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

    boolean existsByFacultyIdAndEvaluatorIdAndSubjectCodeAndSemesterAndSchoolYearAndEvaluationType(String facultyId, String evaluatorId, String subjectCode, String semester, Integer schoolYear, EvaluationType evaluationType);

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
                        ON fes.class_code = pc.class_code
                       AND fes.faculty_id = pc.faculty_id
                       AND fes.school_year = pc.school_year
                       AND fes.semester = pc.semester
                    INNER JOIN primary_section ps
                        ON pc.section_id = ps.section_id
                    WHERE fes.faculty_id = :facultyId
                      AND fes.school_year = :schoolYear
                      AND fes.semester = :semester
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
                ON (fes.evaluator_id) =
                   (ps.student_id)
            INNER JOIN primary_student_load psl
                ON (ps.student_id) =
                   (psl.student_id)
            INNER JOIN primary_class pc
                ON (psl.class_code) =
                   (pc.class_code)
               AND (fes.class_code) =
                   (pc.class_code)
               AND (fes.faculty_id) =
                   (pc.faculty_id)
            INNER JOIN primary_section pse
                ON pc.section_id = pse.section_id
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
                        AND fw_overload.program_code = pse.program_code
                        AND fw_overload.year_level = pse.year_level
                        AND fw_overload.section_code = pse.section_code
                    )
               )
               AND LOWER(TRIM(fw_overload.load_status)) = 'overload'
            INNER JOIN primary_faculty pf
                ON (pc.faculty_id) =
                   (pf.faculty_id)
            WHERE
                fw_overload.faculty_workload_id IS NULL
                AND
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
                ON (fes.evaluator_id) =
                   (ps.student_id)
            INNER JOIN primary_student_load psl
                ON (ps.student_id) =
                   (psl.student_id)
            INNER JOIN primary_class pc
                ON (psl.class_code) =
                   (pc.class_code)
               AND (fes.class_code) =
                   (pc.class_code)
               AND (fes.faculty_id) =
                   (pc.faculty_id)
            INNER JOIN primary_section pse
                ON pc.section_id = pse.section_id
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
                        AND fw_overload.program_code = pse.program_code
                        AND fw_overload.year_level = pse.year_level
                        AND fw_overload.section_code = pse.section_code
                    )
               )
               AND LOWER(TRIM(fw_overload.load_status)) = 'overload'
            INNER JOIN primary_faculty pf
                ON (pc.faculty_id) =
                   (pf.faculty_id)
            WHERE
                fw_overload.faculty_workload_id IS NULL
                AND
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
                    FROM primary_student ps
                    INNER JOIN faculty_evaluation_score fes
                        ON fes.evaluator_id = ps.student_id
                       AND fes.school_year = :schoolYear
                       AND fes.semester = :semester
                       AND (
                            fes.evaluation_type = 'ROLE_STUDENT'
                            OR fes.evaluation_type IS NULL
                       )
                    INNER JOIN primary_faculty pf
                        ON pf.faculty_id = fes.faculty_id
                    WHERE ps.legacy_database = :legacyDatabase
                      AND (
                        :searchTerm IS NULL
                        OR :searchTerm = ''
                        OR LOWER(fes.evaluator_id) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                        OR LOWER(ps.student_firstname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                        OR LOWER(ps.student_lastname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                      )
                    ORDER BY fes.created_at DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM primary_student ps
                    INNER JOIN faculty_evaluation_score fes
                        ON fes.evaluator_id = ps.student_id
                       AND fes.school_year = :schoolYear
                       AND fes.semester = :semester
                       AND (
                            fes.evaluation_type = 'ROLE_STUDENT'
                            OR fes.evaluation_type IS NULL
                       )
                    WHERE ps.legacy_database = :legacyDatabase
                      AND (
                        :searchTerm IS NULL
                        OR :searchTerm = ''
                        OR LOWER(fes.evaluator_id) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                        OR LOWER(ps.student_firstname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                        OR LOWER(ps.student_lastname) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                      )
                    """,
            nativeQuery = true
    )
    Page<Object[]> findEvaluatedStudents(
            @Param("legacyDatabase") String legacyDatabase,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
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
                                (LOWER(TRIM(pc.semester))) =
                                (LOWER(TRIM(:semester)))
                                OR (
                                    UPPER(TRIM(:semester)) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY pc.faculty_id
                    ) cs
                        ON (cs.faculty_id) =
                           (pf.faculty_id)
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
                            ON (sua.user_id) =
                               (fes.evaluator_id)
                        WHERE fes.evaluation_type = 'ROLE_PROGRAM_CHAIR'
                          AND fes.school_year = :schoolYear
                          AND (
                                (LOWER(TRIM(fes.semester))) =
                                (LOWER(TRIM(:semester)))
                                OR (
                                    UPPER(TRIM(:semester)) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY fes.faculty_id
                    ) se
                        ON (se.faculty_id) =
                           (pf.faculty_id)
                    WHERE UPPER(TRIM(pf.status)) = 'ACTIVE'
                      AND (
                            pf.college IS NULL
                            OR UPPER(TRIM(pf.college)) <> 'FOR_MIGRATION'
                      )
                      AND (
                            :legacyDatabase IS NULL
                            OR :legacyDatabase = ''
                            OR (UPPER(TRIM(pf.legacy_database))) =
                               (UPPER(TRIM(:legacyDatabase)))
                      )
                      AND (
                            :campus IS NULL
                            OR :campus = ''
                            OR (UPPER(TRIM(cs.campus))) =
                               (UPPER(TRIM(:campus)))
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
                            OR (LOWER(COALESCE(pf.faculty_id, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.firstname, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.lastname, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.college, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(se.supervisor_names, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
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
                                (LOWER(TRIM(pc.semester))) =
                                (LOWER(TRIM(:semester)))
                                OR (
                                    UPPER(TRIM(:semester)) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY pc.faculty_id
                    ) cs
                        ON (cs.faculty_id) =
                           (pf.faculty_id)
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
                            ON (sua.user_id) =
                               (fes.evaluator_id)
                        WHERE fes.evaluation_type = 'ROLE_PROGRAM_CHAIR'
                          AND fes.school_year = :schoolYear
                          AND (
                                (LOWER(TRIM(fes.semester))) =
                                (LOWER(TRIM(:semester)))
                                OR (
                                    UPPER(TRIM(:semester)) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY fes.faculty_id
                    ) se
                        ON (se.faculty_id) =
                           (pf.faculty_id)
                    WHERE UPPER(TRIM(pf.status)) = 'ACTIVE'
                      AND (
                            pf.college IS NULL
                            OR UPPER(TRIM(pf.college)) <> 'FOR_MIGRATION'
                      )
                      AND (
                            :legacyDatabase IS NULL
                            OR :legacyDatabase = ''
                            OR (UPPER(TRIM(pf.legacy_database))) =
                               (UPPER(TRIM(:legacyDatabase)))
                      )
                      AND (
                            :campus IS NULL
                            OR :campus = ''
                            OR (UPPER(TRIM(cs.campus))) =
                               (UPPER(TRIM(:campus)))
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
                            OR (LOWER(COALESCE(pf.faculty_id, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.firstname, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.lastname, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.college, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(se.supervisor_names, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
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

    @Query(
            value = """
                    SELECT
                        COUNT(*) AS totalFacultyCount,
                        COALESCE(SUM(CASE
                            WHEN COALESCE(se.supervisor_evaluation_count, 0) > 0 THEN 1
                            ELSE 0
                        END), 0) AS evaluatedFacultyCount,
                        COALESCE(SUM(CASE
                            WHEN COALESCE(se.supervisor_evaluation_count, 0) = 0 THEN 1
                            ELSE 0
                        END), 0) AS pendingFacultyCount
                    FROM primary_faculty pf
                    INNER JOIN (
                        SELECT pc.faculty_id, MAX(pc.source_campus) AS campus
                        FROM primary_class pc
                        WHERE pc.class_code IS NOT NULL
                          AND pc.school_year = :schoolYear
                          AND (
                                (LOWER(TRIM(pc.semester))) =
                                (LOWER(TRIM(:semester)))
                                OR (
                                    UPPER(TRIM(:semester)) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(pc.semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY pc.faculty_id
                    ) cs
                        ON (cs.faculty_id) =
                           (pf.faculty_id)
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
                            ON (sua.user_id) =
                               (fes.evaluator_id)
                        WHERE fes.evaluation_type = 'ROLE_PROGRAM_CHAIR'
                          AND fes.school_year = :schoolYear
                          AND (
                                (LOWER(TRIM(fes.semester))) =
                                (LOWER(TRIM(:semester)))
                                OR (
                                    UPPER(TRIM(:semester)) IN ('1ST', 'FIRST_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('1ST', 'FIRST_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('2ND', 'SECOND_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('2ND', 'SECOND_SEMESTER')
                                )
                                OR (
                                    UPPER(TRIM(:semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                    AND UPPER(TRIM(fes.semester)) IN ('SUMMER', 'SUMMER_SEMESTER')
                                )
                          )
                        GROUP BY fes.faculty_id
                    ) se
                        ON (se.faculty_id) =
                           (pf.faculty_id)
                    WHERE UPPER(TRIM(pf.status)) = 'ACTIVE'
                      AND (
                            pf.college IS NULL
                            OR UPPER(TRIM(pf.college)) <> 'FOR_MIGRATION'
                      )
                      AND (
                            :legacyDatabase IS NULL
                            OR :legacyDatabase = ''
                            OR (UPPER(TRIM(pf.legacy_database))) =
                               (UPPER(TRIM(:legacyDatabase)))
                      )
                      AND (
                            :campus IS NULL
                            OR :campus = ''
                            OR (UPPER(TRIM(cs.campus))) =
                               (UPPER(TRIM(:campus)))
                      )
                      AND (
                            :search IS NULL
                            OR :search = ''
                            OR (LOWER(COALESCE(pf.faculty_id, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.firstname, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.lastname, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(pf.college, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                            OR (LOWER(COALESCE(se.supervisor_names, '')))
                               LIKE (LOWER(CONCAT('%', :search, '%')))
                      )
                    """,
            nativeQuery = true
    )
    SupervisorEvaluationDashboardMetricsProjection findSupervisorEvaluationDashboardMetrics(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            @Param("search") String search,
            @Param("legacyDatabase") String legacyDatabase,
            @Param("campus") String campus
    );

}





