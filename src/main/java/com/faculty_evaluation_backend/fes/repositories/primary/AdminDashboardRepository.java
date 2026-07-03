package com.faculty_evaluation_backend.fes.repositories.primary;

import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownProjection;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

@org.springframework.stereotype.Repository
public interface AdminDashboardRepository
        extends Repository<PrimaryClass, Long> {

    @Query(value = """
            SELECT COUNT(DISTINCT ps.student_id)
            FROM primary_student ps
            INNER JOIN primary_student_load psl
                ON ps.student_id = psl.student_id
            INNER JOIN primary_class pc
                ON psl.class_code = pc.class_code
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
            """, nativeQuery = true)
    Long countDistinctStudentsByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COUNT(DISTINCT pc.faculty_id)
            FROM primary_class pc
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
              AND pc.faculty_id IS NOT NULL
            """, nativeQuery = true)
    Long countFacultyByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COUNT(DISTINCT pc.class_code)
            FROM primary_class pc
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
            """, nativeQuery = true)
    Long countClassesByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COUNT(DISTINCT pc.subject_code)
            FROM primary_class pc
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
              AND pc.subject_code IS NOT NULL
            """, nativeQuery = true)
    Long countSubjectsByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COUNT(DISTINCT ps.program_code)
            FROM primary_class pc
            INNER JOIN primary_section ps
                ON pc.section_id = ps.section_id
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
              AND ps.program_code IS NOT NULL
            """, nativeQuery = true)
    Long countProgramsByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COUNT(DISTINCT pc.section_id)
            FROM primary_class pc
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
              AND pc.section_id IS NOT NULL
            """, nativeQuery = true)
    Long countSectionsByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COUNT(DISTINCT psl.primary_student_load_id)
            FROM primary_student ps
            INNER JOIN primary_student_load psl
                ON ps.student_id = psl.student_id
            INNER JOIN primary_class pc
                ON psl.class_code = pc.class_code
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
            """, nativeQuery = true)
    Long countExpectedEvaluationsByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COUNT(DISTINCT fes.faculty_evaluation_score_id)
            FROM faculty_evaluation_score fes
            INNER JOIN primary_student ps
                ON fes.evaluator_id = ps.student_id
            WHERE fes.school_year = :schoolYear
              AND fes.semester = :semester
            """, nativeQuery = true)
    Long countCompletedEvaluationsByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COUNT(DISTINCT fes.evaluator_id)
            FROM faculty_evaluation_score fes
            INNER JOIN primary_student ps
                ON fes.evaluator_id = ps.student_id
            WHERE fes.school_year = :schoolYear
              AND fes.semester = :semester
            """, nativeQuery = true)
    Long countEvaluatedStudentsByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT COALESCE(ROUND(AVG(fes.overall_average_score), 2), 0)
            FROM faculty_evaluation_score fes
            INNER JOIN primary_student ps
                ON fes.evaluator_id = ps.student_id
            WHERE fes.school_year = :schoolYear
              AND fes.semester = :semester
              AND fes.overall_average_score IS NOT NULL
            """, nativeQuery = true)
    Double averageOverallScoreByTerm(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT
                pse.program_code AS programCode,
                COUNT(DISTINCT ps.student_id) AS totalStudents,
                COUNT(DISTINCT pc.class_code) AS totalClasses,
                COUNT(DISTINCT pc.section_id) AS totalSections,
                COUNT(DISTINCT psl.primary_student_load_id) AS expectedEvaluations,
                COUNT(DISTINCT fes.faculty_evaluation_score_id) AS completedEvaluations,
                COALESCE(ROUND(AVG(fes.overall_average_score), 2), 0) AS averageOverallScore
            FROM primary_student ps
            INNER JOIN primary_student_load psl
                ON ps.student_id = psl.student_id
            INNER JOIN primary_class pc
                ON psl.class_code = pc.class_code
            INNER JOIN primary_section pse
                ON pc.section_id = pse.section_id
            LEFT JOIN faculty_evaluation_score fes
                ON fes.evaluator_id = ps.student_id
               AND fes.class_code = pc.class_code
               AND fes.school_year = pc.school_year
               AND fes.semester = pc.semester
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
            GROUP BY pse.program_code
            ORDER BY totalStudents DESC, pse.program_code ASC
            """, nativeQuery = true)
    List<AdminDashboardProgramBreakdownProjection> findProgramBreakdown(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    @Query(value = """
            SELECT
                pc.faculty_id AS facultyId,
                TRIM(CONCAT(
                    COALESCE(pf.firstname, ''),
                    ' ',
                    COALESCE(pf.lastname, '')
                )) AS facultyName,
                COUNT(DISTINCT pc.class_code) AS totalClasses,
                COUNT(DISTINCT pc.subject_code) AS totalSubjects,
                COUNT(DISTINCT psl.student_id) AS totalStudents,
                COUNT(DISTINCT fes.faculty_evaluation_score_id) AS completedEvaluations,
                COALESCE(ROUND(AVG(fes.overall_average_score), 2), 0) AS averageOverallScore
            FROM primary_class pc
            LEFT JOIN primary_faculty pf
                ON pc.faculty_id = pf.faculty_id
            LEFT JOIN primary_student_load psl
                ON pc.class_code = psl.class_code
            LEFT JOIN primary_student ps
                ON psl.student_id = ps.student_id
            LEFT JOIN faculty_evaluation_score fes
                ON fes.faculty_id = pc.faculty_id
               AND fes.class_code = pc.class_code
               AND fes.evaluator_id = ps.student_id
               AND fes.school_year = pc.school_year
               AND fes.semester = pc.semester
            WHERE pc.school_year = :schoolYear
              AND pc.semester = :semester
              AND pc.faculty_id IS NOT NULL
            GROUP BY pc.faculty_id, pf.firstname, pf.lastname
            ORDER BY totalStudents DESC, totalClasses DESC, facultyName ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<AdminDashboardFacultyLoadProjection> findTopFacultyLoads(
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester,
            @Param("limit") int limit
    );
}
