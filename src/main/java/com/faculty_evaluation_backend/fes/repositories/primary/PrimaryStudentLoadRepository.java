package com.faculty_evaluation_backend.fes.repositories.primary;


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

    @Query("SELECT CASE WHEN COUNT(sl) > 0 THEN true ELSE false END FROM PrimaryStudentLoad sl " +
            "WHERE sl.loadId = :loadId " +
            "AND sl.studentId = :studentId " +
            "AND (sl.yearLevel = :yearLevel OR (sl.yearLevel IS NULL AND :yearLevel IS NULL)) " +
            "AND sl.classCode = :classCode")
    boolean existsByLoadIdAndStudentIdAndYearLevelAndClassCode(
            @Param("loadId") Integer loadId,
            @Param("studentId") String studentId,
            @Param("yearLevel") String yearLevel,
            @Param("classCode") Integer classCode
    );

    Integer countDistinctTotalPrimaryStudentLoadByStudentId(String studentId);

    @Query(
            value = """
        SELECT DISTINCT new com.faculty_evaluation_backend.fes.dto.student.StudentClassLoadDTO(
            pc.classCode,
            pc.facultyId,
            pc.subjectCode,
            pc.sectionId,
            pc.semester,
            pc.schoolYear,
            psl.yearLevel,
            psl.studentId,
            CAST(f.college AS string),
            CONCAT(
                COALESCE(f.firstname, ''),
                CASE
                    WHEN f.firstname IS NOT NULL
                     AND f.lastname IS NOT NULL
                    THEN ' '
                    ELSE ''
                END,
                COALESCE(f.lastname, '')
            ),
            s.descriptiveTitle
        )
        FROM PrimaryStudentLoad psl

        LEFT JOIN PrimaryClass pc
            ON psl.classCode = pc.classCode
           AND pc.schoolYear = :schoolYear
           AND pc.semester = :semester

        LEFT JOIN PrimaryFaculty f
            ON pc.facultyId = f.facultyId

        LEFT JOIN PrimarySubject s
            ON pc.subjectCode = s.subjectCode

        WHERE psl.studentId = :studentId

        ORDER BY pc.classCode ASC
    """,

            countQuery = """
        SELECT COUNT(DISTINCT psl.primaryStudentLoadId)
        FROM PrimaryStudentLoad psl

        LEFT JOIN PrimaryClass pc
            ON psl.classCode = pc.classCode
           AND pc.schoolYear = :schoolYear
           AND pc.semester = :semester

        WHERE psl.studentId = :studentId
    """
    )
    Page<StudentClassLoadDTO> findStudentLoadDTO(
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
