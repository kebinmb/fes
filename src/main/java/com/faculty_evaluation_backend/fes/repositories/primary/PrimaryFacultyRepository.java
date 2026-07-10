package com.faculty_evaluation_backend.fes.repositories.primary;

import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageProjection;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PrimaryFacultyRepository extends JpaRepository<PrimaryFaculty, Long> {

    Optional<PrimaryFaculty> findByFacultyId(String facultyId);

    boolean existsByFacultyId(String facultyId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM PrimaryFaculty f " +
            "WHERE f.facultyId = :facultyId " +
            "AND f.firstname = :firstname " +
            "AND f.lastname = :lastname " +
            "AND f.position = :position " +
            "AND f.loadLimit = :loadLimit " +
            "AND (f.middlename = :middlename OR (f.middlename IS NULL AND :middlename IS NULL))")
    boolean existsByFacultyIdAndFirstnameAndLastnameAndPositionAndLoadLimitAndMiddlename(
            @Param("facultyId") String facultyId,
            @Param("firstname") String firstname,
            @Param("lastname") String lastname,
            @Param("position") String position,
            @Param("loadLimit") Double loadLimit,
            @Param("middlename") String middlename
    );

    List<PrimaryFaculty> findByCollegeAndStatus(College college, Status status);

    @Query("""
            SELECT f
            FROM PrimaryFaculty f
            WHERE f.status = :status
              AND (
                    :legacyDatabase IS NULL
                    OR :legacyDatabase = ''
                    OR f.legacyDatabase = :legacyDatabase
              )
            ORDER BY f.lastname ASC, f.firstname ASC, f.facultyId ASC
            """)
    List<PrimaryFaculty> findAssignmentOptions(
            @Param("status") Status status,
            @Param("legacyDatabase") String legacyDatabase
    );

    @Query(
            value = """
                    SELECT
                        f.faculty_id AS facultyId,
                        f.firstname AS firstname,
                        f.middlename AS middlename,
                        f.lastname AS lastname,
                        f.position AS position,
                        f.college AS college,
                        f.status AS status,
                        f.load_limit AS loadLimit,
                        COUNT(DISTINCT fw.faculty_workload_id) AS workloadCount,
                        COALESCE(SUM(fw.total_hours_per_week), 0) AS totalHoursPerWeek,
                        MAX(fw.number_of_preparations) AS numberOfPreparations
                    FROM primary_faculty f
                    LEFT JOIN faculty_workload fw
                        ON fw.faculty_id = f.faculty_id
                       AND fw.school_year = :schoolYear
                       AND (
                            LOWER(TRIM(fw.semester)) = LOWER(TRIM(:semester))
                            OR UPPER(TRIM(fw.semester)) = UPPER(TRIM(:workloadSemester))
                       )
                    WHERE f.status = :status
                      AND (
                            :excludedCollege IS NULL
                            OR f.college IS NULL
                            OR f.college <> :excludedCollege
                      )
                    GROUP BY
                        f.faculty_id,
                        f.firstname,
                        f.middlename,
                        f.lastname,
                        f.position,
                        f.college,
                        f.status,
                        f.load_limit
                    ORDER BY f.lastname ASC, f.firstname ASC, f.faculty_id ASC
                    """,
            nativeQuery = true
    )
    List<FacultyWorkloadCoverageProjection> findFacultyWorkloadCoverage(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            @Param("workloadSemester") String workloadSemester,
            @Param("status") String status,
            @Param("excludedCollege") String excludedCollege
    );

    @Transactional
    @Modifying
    @Query("""
                UPDATE PrimaryFaculty f
                SET
                    f.firstname = :firstname,
                    f.middlename = :middlename,
                    f.lastname = :lastname,
                    f.position = :position,
                    f.loadLimit = :loadLimit,
                    f.college = :college,
                    f.status = :status
                WHERE f.facultyId = :facultyId
            """)
    int updateFaculty(
            @Param("facultyId") String facultyId,
            @Param("firstname") String firstname,
            @Param("middlename") String middlename,
            @Param("lastname") String lastname,
            @Param("position") String position,
            @Param("loadLimit") Double loadLimit,
            @Param("college") College college,
            @Param("status") Status status
    );

    @Query("""
                SELECT f
                FROM PrimaryFaculty f
                WHERE (f.college IS NULL OR f.college <> :excludedCollege)
                  AND (
                        :legacyDatabase IS NULL
                        OR :legacyDatabase = ''
                        OR f.legacyDatabase = :legacyDatabase
                  )
                  AND (
                        :search IS NULL
                        OR :search = ''
                        OR LOWER(COALESCE(f.firstname, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(COALESCE(f.lastname, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(COALESCE(f.facultyId, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(COALESCE(f.position, '')) LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(CONCAT(COALESCE(f.firstname, ''), ' ', COALESCE(f.lastname, '')))
                           LIKE LOWER(CONCAT('%', :search, '%'))
                        OR LOWER(CONCAT(COALESCE(f.lastname, ''), ', ', COALESCE(f.firstname, '')))
                           LIKE LOWER(CONCAT('%', :search, '%'))
                  )
            """)
    Page<PrimaryFaculty> searchExcludingCollege(
            @Param("search") String search,
            @Param("excludedCollege") College excludedCollege,
            @Param("legacyDatabase") String legacyDatabase,
            Pageable pageable
    );

    boolean existsByLegacyDatabaseAndLegacyId(
            String legacyDatabase,
            String legacyId
    );

    @Query("""
                SELECT f.legacyId
                FROM PrimaryFaculty f
                WHERE f.legacyDatabase = :database
            """)
    Set<String> findLegacyIdsByDatabase(@Param("database") String database);
}
