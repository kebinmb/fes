package com.faculty_evaluation_backend.fes.repositories.primary;

import com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudentLoad;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PrimaryStudentLoadRepository
        extends JpaRepository<PrimaryStudentLoad, Long> {

    @Query("""
            SELECT CASE
                WHEN COUNT(sl) > 0 THEN true
                ELSE false
            END
            FROM PrimaryStudentLoad sl
            WHERE sl.loadId = :loadId
              AND sl.studentId = :studentId
              AND (
                    sl.yearLevel = :yearLevel
                    OR (
                        sl.yearLevel IS NULL
                        AND :yearLevel IS NULL
                    )
                  )
              AND sl.classCode = :classCode
            """)
    boolean existsByLoadIdAndStudentIdAndYearLevelAndClassCode(
            @Param("loadId") Integer loadId,
            @Param("studentId") String studentId,
            @Param("yearLevel") String yearLevel,
            @Param("classCode") Integer classCode
    );

    Integer countDistinctTotalPrimaryStudentLoadByStudentId(
            String studentId
    );

    @Query(
            value = """
                    SELECT
                        pc.class_code AS classCode,
                        pc.faculty_id AS facultyId,
                        pc.subject_code AS subjectCode,
                        pc.section_id AS sectionId,
                        pc.semester AS semester,
                        pc.school_year AS schoolYear,
                        psl.year_level AS yearLevel,
                        psl.student_id AS studentId,
                        CAST(f.college AS CHAR) AS college,
                        CONCAT(
                            COALESCE(f.firstname, ''),
                            ' ',
                            COALESCE(f.lastname, '')
                        ) AS facultyName,
                        s.descriptive_title AS subjectDescription

                    FROM primary_student_load psl

                    LEFT JOIN primary_class pc
                        ON psl.class_code = pc.class_code

                    LEFT JOIN primary_faculty f
                        ON pc.faculty_id = f.faculty_id

                    LEFT JOIN primary_subject s
                        ON pc.subject_code = s.subject_code

                    WHERE psl.student_id = :studentId
                      AND pc.school_year = :schoolYear
                      AND pc.semester = :semester

                    ORDER BY pc.subject_code ASC
                    """,

            countQuery = """
                    SELECT COUNT(*)

                    FROM primary_student_load psl

                    LEFT JOIN primary_class pc
                        ON psl.class_code = pc.class_code

                    WHERE psl.student_id = :studentId
                      AND pc.school_year = :schoolYear
                      AND pc.semester = :semester
                    """,

            nativeQuery = true
    )
    Page<Object[]> findStudentLoadDTO(
            @Param("studentId") String studentId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(psl)
            FROM PrimaryStudentLoad psl
            WHERE psl.classCode = :classCode
            """)
    Integer findTotalStudentsInClass(
            @Param("classCode") String classCode
    );

    boolean existsByLegacyDatabaseAndLegacyId(
            String legacyDatabase,
            String legacyId
    );
}