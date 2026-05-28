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

@Repository
public interface PrimaryStudentLoadRepository extends JpaRepository<PrimaryStudentLoad, Long> {

    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END FROM PrimaryStudentLoad sl " + "WHERE sl.loadId = :loadId " + "AND sl.studentId = :studentId " + "AND (sl.yearLevel = :yearLevel OR (sl.yearLevel IS NULL AND :yearLevel IS NULL)) " + "AND sl.classCode = :classCode")
    boolean existsByLoadIdAndStudentIdAndYearLevelAndClassCode(@Param("loadId") Integer loadId, @Param("studentId") String studentId, @Param("yearLevel") String yearLevel, @Param("classCode") Integer classCode);

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
        ON psl.class_code = pc.class_code

    INNER JOIN primary_section ps
        ON ps.section_id = pc.section_id

    LEFT JOIN faculty_evaluation_score fes
        ON fes.evaluator_id = psl.student_id
        AND fes.class_code = psl.class_code

    WHERE
        ps.program_code = :programCode
        AND ps.year_level = :yearLevel
        AND ps.section_code = :sectionCode

    ORDER BY
        psl.student_id,
        pc.subject_code
    """, nativeQuery = true)
    List<StudentEvaluationStatusResponse>
    fetchStudentEvaluationStatus(
            @Param("programCode") String programCode,
            @Param("yearLevel") String yearLevel,
            @Param("sectionCode") String sectionCode
    );
}
