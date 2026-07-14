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

    @EntityGraph(attributePaths = "faculty")
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
                        ON pf.faculty_id = fw.faculty_id
                    INNER JOIN user_accounts ua
                        ON ua.user_id = :userId
                       AND UPPER(TRIM(ua.data_source)) =
                           UPPER(TRIM(pf.legacy_database))
                    WHERE fw.faculty_id = :facultyId
                      AND fw.school_year = :schoolYear
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
                    SELECT fw
                    FROM FacultyWorkload fw
                    LEFT JOIN fw.faculty f
                    WHERE (
                            :search IS NULL
                            OR :search = ''
                            OR LOWER(fw.facultyId) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(fw.courseCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(fw.programCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(fw.yearLevel, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(fw.sectionCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(f.firstname, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(f.lastname, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(CONCAT(COALESCE(f.firstname, ''), ' ', COALESCE(f.lastname, '')))
                               LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(CONCAT(COALESCE(f.lastname, ''), ', ', COALESCE(f.firstname, '')))
                               LIKE LOWER(CONCAT('%', :search, '%'))
                    )
                      AND (:schoolYear IS NULL OR fw.schoolYear = :schoolYear)
                      AND (
                            :semester IS NULL
                            OR :semester = ''
                            OR LOWER(TRIM(fw.semester)) = LOWER(TRIM(:semester))
                      )
                    """,
            countQuery = """
                    SELECT COUNT(fw)
                    FROM FacultyWorkload fw
                    LEFT JOIN fw.faculty f
                    WHERE (
                            :search IS NULL
                            OR :search = ''
                            OR LOWER(fw.facultyId) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(fw.courseCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(fw.programCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(fw.yearLevel, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(fw.sectionCode, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(f.firstname, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(COALESCE(f.lastname, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(CONCAT(COALESCE(f.firstname, ''), ' ', COALESCE(f.lastname, '')))
                               LIKE LOWER(CONCAT('%', :search, '%'))
                            OR LOWER(CONCAT(COALESCE(f.lastname, ''), ', ', COALESCE(f.firstname, '')))
                               LIKE LOWER(CONCAT('%', :search, '%'))
                    )
                      AND (:schoolYear IS NULL OR fw.schoolYear = :schoolYear)
                      AND (
                            :semester IS NULL
                            OR :semester = ''
                            OR LOWER(TRIM(fw.semester)) = LOWER(TRIM(:semester))
                      )
                    """
    )
    Page<FacultyWorkload> searchWorkloads(
            @Param("search") String search,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            Pageable pageable
    );
}
