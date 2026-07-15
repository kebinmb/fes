package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.dto.evaluation.StudentEvaluationStatusResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudentLoad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PrimaryStudentLoadRepository extends JpaRepository<PrimaryStudentLoad, Long> {

    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END FROM PrimaryStudentLoad sl " + "WHERE sl.loadId = :loadId " + "AND sl.studentId = :studentId " + "AND (sl.yearLevel = :yearLevel OR (sl.yearLevel IS NULL AND :yearLevel IS NULL)) " + "AND sl.classCode = :classCode")
    boolean existsByLoadIdAndStudentIdAndYearLevelAndClassCode(@Param("loadId") Integer loadId, @Param("studentId") String studentId, @Param("yearLevel") String yearLevel, @Param("classCode") String classCode);

    Integer countDistinctTotalPrimaryStudentLoadByStudentId(String studentId);

    @Query("""
                SELECT new com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO(
                    pc.classCode,
                    pc.facultyId,
                    pc.subjectCode,
                    pc.sectionId,
                    pc.semester,
                    pc.schoolYear,
                    psl.yearLevel,
                    psl.studentId,
                    CAST(f.college AS string),
                    CONCAT(f.firstname, ' ', f.lastname),
                    s.descriptiveTitle
                )
                FROM PrimaryStudentLoad psl
                LEFT JOIN psl.primaryClass pc
                LEFT JOIN pc.faculty f
                LEFT JOIN pc.subject s
                WHERE psl.studentId = :studentId
                  AND (
                        (pc.schoolYear = :schoolYear AND pc.semester = :semester)
                        OR pc IS NULL
                      )
            """)
    Page<StudentClassLoadDTO> findStudentLoadDTO(@Param("studentId") String studentId, @Param("schoolYear") Integer schoolYear, @Param("semester") String semester, Pageable pageable);

    @Query("""
                SELECT COUNT(psl)
                FROM PrimaryStudentLoad psl
                WHERE psl.classCode = :classCode
            """)
    Integer findTotalStudentsInClass(@Param("classCode") String classCode);

    boolean existsByLegacyDatabaseAndLegacyId(String legacyDatabase, String legacyId);

    @Query("""
                SELECT psl.legacyId
                FROM PrimaryStudentLoad psl
                WHERE psl.legacyDatabase = :database
            """)
    Set<String> findLegacyIdsByDatabase(@Param("database") String database);

    @Query(value = """
    SELECT
        psl.student_id AS studentId,
        ps.program_code AS programCode,
        ps.year_level AS yearLevel,
        ps.section_code AS sectionCode,
        pc.subject_code AS subjectCode,

        COALESCE(
            DATE_FORMAT(
                fes.created_at,
                '%Y-%m-%d %H:%i:%s'
            ),
            'NOT_EVALUATED'
        ) AS createdAt,

        CASE
            WHEN fes.evaluator_id IS NOT NULL
            THEN 'EVALUATED'
            ELSE 'NOT_EVALUATED'
        END AS evaluationStatus

    FROM primary_student_load psl

    INNER JOIN primary_class pc
        ON (CAST(psl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
           (CAST(pc.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)

    INNER JOIN primary_section ps
        ON ps.section_id = pc.section_id

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
        ON (CAST(fes.evaluator_id AS CHAR) COLLATE utf8mb4_unicode_ci) =
           (CAST(psl.student_id AS CHAR) COLLATE utf8mb4_unicode_ci)
        AND (CAST(fes.class_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
            (CAST(psl.class_code AS CHAR) COLLATE utf8mb4_unicode_ci)
        AND fes.school_year = pc.school_year
        AND (CAST(fes.semester AS CHAR) COLLATE utf8mb4_unicode_ci) =
            (CAST(pc.semester AS CHAR) COLLATE utf8mb4_unicode_ci)

    WHERE
        (CAST(ps.program_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
        (CAST(:programCode AS CHAR) COLLATE utf8mb4_unicode_ci)
        AND (CAST(ps.year_level AS CHAR) COLLATE utf8mb4_unicode_ci) =
            (CAST(:yearLevel AS CHAR) COLLATE utf8mb4_unicode_ci)
        AND (CAST(ps.section_code AS CHAR) COLLATE utf8mb4_unicode_ci) =
            (CAST(:sectionCode AS CHAR) COLLATE utf8mb4_unicode_ci)
        AND pc.school_year = :schoolYear
        AND (LOWER(TRIM(CAST(pc.semester AS CHAR))) COLLATE utf8mb4_unicode_ci) =
            (LOWER(TRIM(CAST(:semester AS CHAR))) COLLATE utf8mb4_unicode_ci)
        AND fw_overload.faculty_workload_id IS NULL

    ORDER BY
        psl.student_id,
        pc.subject_code
    """, nativeQuery = true)
    List<StudentEvaluationStatusResponse>
    fetchStudentEvaluationStatus(
            @Param("programCode") String programCode,
            @Param("yearLevel") String yearLevel,
            @Param("sectionCode") String sectionCode,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );
}
