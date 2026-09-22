package com.faculty_evaluation_backend.fes.repositories.primary;


import com.faculty_evaluation_backend.fes.dto.evaluation.StudentEvaluationStatusProjection;
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
        ON (psl.class_code) =
           (pc.class_code)

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
        ON (fes.evaluator_id) =
           (psl.student_id)
        AND (fes.class_code) =
            (psl.class_code)
        AND fes.faculty_id = pc.faculty_id
        AND fes.subject_code = pc.subject_code
        AND fes.year_level = ps.year_level
        AND fes.school_year = pc.school_year
        AND (fes.semester) =
            (pc.semester)
        AND (
            fes.evaluation_type = 'ROLE_STUDENT'
            OR fes.evaluation_type IS NULL
        )

    WHERE
        (ps.program_code) =
        (:programCode)
        AND (ps.year_level) =
            (:yearLevel)
        AND (ps.section_code) =
            (:sectionCode)
        AND pc.school_year = :schoolYear
        AND (LOWER(TRIM(pc.semester))) =
            (LOWER(TRIM(:semester)))
        AND fw_overload.faculty_workload_id IS NULL

    ORDER BY
        psl.student_id,
        pc.subject_code
    """, nativeQuery = true)
    List<StudentEvaluationStatusProjection>
    fetchStudentEvaluationStatus(
            @Param("programCode") String programCode,
            @Param("yearLevel") String yearLevel,
            @Param("sectionCode") String sectionCode,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );
}


