package com.faculty_evaluation_backend.fes.repositories.primary;

import com.faculty_evaluation_backend.fes.entities.primary.FacultyWorkload;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyWorkloadRepository
        extends JpaRepository<FacultyWorkload, Long> {

    @EntityGraph(attributePaths = "faculty")
    Optional<FacultyWorkload> findByFacultyIdAndSchoolYearAndSemester(
            String facultyId,
            Integer schoolYear,
            String semester
    );

    @EntityGraph(attributePaths = "faculty")
    List<FacultyWorkload> findAllByFacultyIdAndSchoolYearAndSemester(
            String facultyId,
            Integer schoolYear,
            String semester
    );

    @Query(
            value = """
                    SELECT fw
                    FROM FacultyWorkload fw
                    WHERE fw.facultyId = :facultyId
                      AND fw.schoolYear = :schoolYear
                      AND (
                            LOWER(TRIM(fw.semester)) = LOWER(TRIM(:semester))
                            OR (
                                UPPER(TRIM(:semester)) IN ('1ST', 'FIRST_SEMESTER')
                                AND UPPER(TRIM(fw.semester)) IN ('1ST', 'FIRST_SEMESTER')
                            )
                            OR (
                                UPPER(TRIM(:semester)) IN ('2ND', 'SECOND_SEMESTER')
                                AND UPPER(TRIM(fw.semester)) IN ('2ND', 'SECOND_SEMESTER')
                            )
                      )
                    """
    )
    List<FacultyWorkload> findAllByFacultyIdAndSchoolYearAndEquivalentSemester(
            @Param("facultyId") String facultyId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(
            value = """
                    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
                    FROM faculty_workload fw
                    INNER JOIN primary_faculty pf
                        ON (CAST(pf.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(fw.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    INNER JOIN user_accounts ua
                        ON ua.user_id = :userId
                       AND (UPPER(TRIM(CAST(ua.data_source AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                           (UPPER(TRIM(CAST(pf.legacy_database AS CHAR))) COLLATE utf8mb4_unicode_ci)
                    WHERE (CAST(fw.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                          (CAST(:facultyId AS CHAR) COLLATE utf8mb4_unicode_ci)
                      AND fw.school_year = :schoolYear
                      AND (
                            (LOWER(TRIM(CAST(fw.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                            (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                            OR (
                                UPPER(TRIM(CAST(:semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                                AND UPPER(TRIM(CAST(fw.semester AS CHAR))) IN ('1ST', 'FIRST_SEMESTER')
                            )
                            OR (
                                UPPER(TRIM(CAST(:semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                                AND UPPER(TRIM(CAST(fw.semester AS CHAR))) IN ('2ND', 'SECOND_SEMESTER')
                            )
                      )
                    """,
            nativeQuery = true
    )
    boolean existsFacultyWorkloadInSupervisorScope(
            @Param("userId") Long userId,
            @Param("facultyId") String facultyId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @EntityGraph(attributePaths = "faculty")
    Optional<FacultyWorkload>
    findByFacultyIdAndSchoolYearAndSemesterAndClassCodeAndCourseCodeAndProgramCodeAndYearLevelAndSectionCode(
            String facultyId,
            Integer schoolYear,
            String semester,
            String classCode,
            String courseCode,
            String programCode,
            String yearLevel,
            String sectionCode
    );

    @EntityGraph(attributePaths = "faculty")
    Optional<FacultyWorkload>
    findByFacultyIdAndSchoolYearAndSemesterAndCourseCodeAndProgramCodeAndYearLevelAndSectionCode(
            String facultyId,
            Integer schoolYear,
            String semester,
            String courseCode,
            String programCode,
            String yearLevel,
            String sectionCode
    );

    @Query(
            value = """
                    SELECT fw.*
                    FROM faculty_workload fw
                    LEFT JOIN primary_faculty f
                        ON (CAST(f.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(fw.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    WHERE (
                            :search IS NULL
                            OR :search = ''
                            OR (LOWER(COALESCE(CAST(fw.faculty_id AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(fw.course_code AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(fw.program_code AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(fw.year_level AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(fw.section_code AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(f.firstname AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(f.lastname AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR LOWER(CONCAT(COALESCE(f.firstname, ''), ' ', COALESCE(f.lastname, '')))
                               COLLATE utf8mb4_unicode_ci LIKE
                               (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR LOWER(CONCAT(COALESCE(f.lastname, ''), ', ', COALESCE(f.firstname, '')))
                               COLLATE utf8mb4_unicode_ci LIKE
                               (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                    )
                      AND (:schoolYear IS NULL OR fw.school_year = :schoolYear)
                      AND (
                            :semester IS NULL
                            OR :semester = ''
                            OR (LOWER(TRIM(CAST(fw.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                               (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                      )
                    ORDER BY fw.updated_at DESC, fw.faculty_workload_id DESC
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM faculty_workload fw
                    LEFT JOIN primary_faculty f
                        ON (CAST(f.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
                           (CAST(fw.faculty_id AS CHAR) COLLATE utf8mb4_unicode_ci)
                    WHERE (
                            :search IS NULL
                            OR :search = ''
                            OR (LOWER(COALESCE(CAST(fw.faculty_id AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(fw.course_code AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(fw.program_code AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(fw.year_level AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(fw.section_code AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(f.firstname AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR (LOWER(COALESCE(CAST(f.lastname AS CHAR), '')) COLLATE utf8mb4_unicode_ci)
                               LIKE (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR LOWER(CONCAT(COALESCE(f.firstname, ''), ' ', COALESCE(f.lastname, '')))
                               COLLATE utf8mb4_unicode_ci LIKE
                               (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                            OR LOWER(CONCAT(COALESCE(f.lastname, ''), ', ', COALESCE(f.firstname, '')))
                               COLLATE utf8mb4_unicode_ci LIKE
                               (LOWER(CONCAT('%', CAST(:search AS CHAR), '%')) COLLATE utf8mb4_unicode_ci)
                    )
                      AND (:schoolYear IS NULL OR fw.school_year = :schoolYear)
                      AND (
                            :semester IS NULL
                            OR :semester = ''
                            OR (LOWER(TRIM(CAST(fw.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
                               (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
                      )
                    """,
            nativeQuery = true
    )
    Page<FacultyWorkload> searchWorkloads(
            @Param("search") String search,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            Pageable pageable
    );
}
