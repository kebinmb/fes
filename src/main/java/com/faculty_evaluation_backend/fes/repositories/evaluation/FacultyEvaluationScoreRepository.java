package com.faculty_evaluation_backend.fes.repositories.evaluation;


import com.faculty_evaluation_backend.fes.dto.evaluation.StudentFacultyEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDetailsDTO;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
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

    boolean existsByFacultyIdAndEvaluatorIdAndClassCodeAndSubjectCodeAndYearLevelAndSemesterAndSchoolYear(
            String facultyId,
            String evaluatorId,
            String classCode,
            String subjectCode,
            String yearLevel,
            String semester,
            Integer schoolYear
    );

    Integer countDistinctEvaluatedSubjectsByEvaluatorId(String evaluatorId);

    List<FacultyEvaluationScore> findByFacultyId(String facultyId);

    List<FacultyEvaluationScore> findByFacultyIdAndClassCode(String facultyId, String classCode);

    @Query("""
       SELECT DISTINCT new com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDetailsDTO(
           f.classCode,
           ps.sectionCode,
           ps.programCode,
           ps.yearLevel
       )
       FROM FacultyEvaluationScore f
       INNER JOIN PrimaryClass pc
           ON f.classCode = pc.classCode
       INNER JOIN PrimarySection ps
           ON pc.sectionId = ps.sectionId
       WHERE pc.facultyId = :facultyId
       AND pc.schoolYear = :schoolYear
       AND pc.semester = :semester
       """)
    List<FacultyClassDetailsDTO>
    findDistinctClassDetailsByFacultyIdAndSchoolYearAndSemester(
            @Param("facultyId") String facultyId,
            @Param("schoolYear") Integer schoolYear,
            @Param("semester") String semester
    );

    List<FacultyEvaluationScore>
    findByFacultyIdAndClassCodeAndSchoolYearAndSemester(
            String facultyId,
            String classCode,
            Integer schoolYear,
            String semester
    );

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
                ON fes.evaluator_id = ps.student_id
            INNER JOIN primary_student_load psl
                ON ps.student_id = psl.student_id
            INNER JOIN primary_class pc
                ON psl.class_code = pc.class_code
            INNER JOIN primary_section pse
                ON pc.section_id = pse.section_id
            INNER JOIN primary_faculty pf
                ON pc.faculty_id = pf.faculty_id
            WHERE
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
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM faculty_evaluation_score fes
            INNER JOIN primary_student ps
                ON fes.evaluator_id = ps.student_id
            INNER JOIN primary_student_load psl
                ON ps.student_id = psl.student_id
            INNER JOIN primary_class pc
                ON psl.class_code = pc.class_code
            INNER JOIN primary_section pse
                ON pc.section_id = pse.section_id
            INNER JOIN primary_faculty pf
                ON pc.faculty_id = pf.faculty_id
            WHERE
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
            """,
            nativeQuery = true)
    Page<Object[]> findStudentFacultyEvaluationDetails(
            @Param("search") String search,
            Pageable pageable
    );
}

