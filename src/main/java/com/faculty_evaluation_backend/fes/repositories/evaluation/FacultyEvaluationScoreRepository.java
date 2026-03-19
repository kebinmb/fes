package com.faculty_evaluation_backend.fes.repositories.evaluation;


import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacultyEvaluationScoreRepository extends JpaRepository<FacultyEvaluationScore, Long> {

    boolean existsByFacultyIdAndEvaluatorIdAndClassCodeAndSemesterAndSchoolYear(
            String facultyId,
            String evaluatorId,
            String classCode,
            String semester,
            Integer schoolYear
    );

    boolean existsByFacultyIdAndEvaluatorIdAndClassCode(
            String facultyId,
            String evaluatorId,
            String classCode
    );
    boolean existsByFacultyIdAndEvaluatorIdAndSubjectCodeAndSemesterAndSchoolYear(
            String facultyId,
            String evaluatorId,
            String subjectCode,
            String semester,
            Integer schoolYear
    );
    Integer countDistinctEvaluatedSubjectsByEvaluatorId(String evaluatorId);

    List<FacultyEvaluationScore> findByFacultyId(String facultyId);

    List<FacultyEvaluationScore> findByFacultyIdAndSemesterAndSchoolYear(
            String facultyId,
            String semester,
            Integer schoolYear
    );
    List<FacultyEvaluationScore> findByFacultyIdAndSubjectCodeAndSemesterAndSchoolYear(
            String facultyId,
            String semester,
            Integer schoolYear,
            String subjectCode
    );
    List<FacultyEvaluationScore> findByFacultyIdAndClassCode(
            String facultyId,
            String classCode
    );


    List<FacultyEvaluationScore> findByEvaluatorId(String evaluatorId);


    List<FacultyEvaluationScore> findByEvaluatorIdAndSemesterAndSchoolYear(
            String evaluatorId,
            String semester,
            Integer schoolYear
    );


    Optional<FacultyEvaluationScore> findByFacultyIdAndEvaluatorIdAndClassCodeAndSemesterAndSchoolYear(
            String facultyId,
            String evaluatorId,
            String classCode,
            String semester,
            Integer schoolYear
    );

    List<FacultyEvaluationScore> findByClassCode(String classCode);

    List<FacultyEvaluationScore> findByClassCodeAndSemesterAndSchoolYear(
            String classCode,
            String semester,
            Integer schoolYear
    );


    List<FacultyEvaluationScore> findBySemesterAndSchoolYear(
            String semester,
            Integer schoolYear
    );


    @Query("SELECT AVG(e.overallAverageScore) FROM FacultyEvaluationScore e " +
           "WHERE e.facultyId = :facultyId")
    Double getAverageScoreForFaculty(@Param("facultyId") String facultyId);


    @Query("SELECT AVG(e.overallAverageScore) FROM FacultyEvaluationScore e " +
           "WHERE e.facultyId = :facultyId AND e.semester = :semester AND e.schoolYear = :schoolYear")
    Double getAverageScoreForFacultyInPeriod(
            @Param("facultyId") String facultyId,
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear
    );


    @Query("SELECT COUNT(e) FROM FacultyEvaluationScore e WHERE e.facultyId = :facultyId")
    Long countEvaluationsByFaculty(@Param("facultyId") String facultyId);


    Long countByFacultyIdAndSemesterAndSchoolYear(
            String facultyId,
            String semester,
            Integer schoolYear
    );

    @Query("SELECT COUNT(DISTINCT e.evaluatorId) FROM FacultyEvaluationScore e " +
           "WHERE e.facultyId = :facultyId AND e.semester = :semester AND e.schoolYear = :schoolYear")
    Long countDistinctStudentsByFacultyIdAndSemesterAndSchoolYear(
            @Param("facultyId") String facultyId,
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear
    );


    @Query("SELECT e.facultyId, AVG(e.overallAverageScore), COUNT(e) " +
           "FROM FacultyEvaluationScore e " +
           "WHERE e.semester = :semester AND e.schoolYear = :schoolYear " +
           "GROUP BY e.facultyId " +
           "HAVING COUNT(e) >= :minEvaluations " +
           "ORDER BY AVG(e.overallAverageScore) DESC")
    List<Object[]> findTopRatedFaculty(
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear,
            @Param("minEvaluations") long minEvaluations
    );


    @Query("SELECT e.facultyId, AVG(e.overallAverageScore) as avgScore " +
           "FROM FacultyEvaluationScore e " +
           "WHERE e.semester = :semester AND e.schoolYear = :schoolYear " +
           "GROUP BY e.facultyId " +
           "ORDER BY avgScore DESC")
    List<Object[]> findFacultyRankings(
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear
    );


    @Query("SELECT e.facultyId FROM FacultyEvaluationScore e " +
           "WHERE e.semester = :semester AND e.schoolYear = :schoolYear " +
           "GROUP BY e.facultyId " +
           "HAVING AVG(e.overallAverageScore) >= 3.0")
    List<String> findPassedFaculty(
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear
    );


    @Query("SELECT e.facultyId FROM FacultyEvaluationScore e " +
           "WHERE e.semester = :semester AND e.schoolYear = :schoolYear " +
           "GROUP BY e.facultyId " +
           "HAVING AVG(e.overallAverageScore) >= 4.5")
    List<String> findExcellentFaculty(
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear
    );


    @Query("SELECT e.facultyId, AVG(e.overallAverageScore) " +
           "FROM FacultyEvaluationScore e " +
           "WHERE e.semester = :semester AND e.schoolYear = :schoolYear " +
           "GROUP BY e.facultyId " +
           "HAVING AVG(e.overallAverageScore) < 3.0")
    List<Object[]> findFacultyNeedingImprovement(
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear
    );


    @Query("SELECT e FROM FacultyEvaluationScore e " +
           "WHERE e.overallInterpretation = 'Incomplete'")
    List<FacultyEvaluationScore> findIncompleteEvaluations();


    @Query("SELECT COUNT(e) FROM FacultyEvaluationScore e " +
           "WHERE e.semester = :semester AND e.schoolYear = :schoolYear " +
           "AND e.overallInterpretation = 'Incomplete'")
    Long countIncompleteEvaluations(
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear
    );

    // ==================== Delete Operations ====================

    /**
     * Delete evaluations by faculty ID (admin operation)
     */
    void deleteByFacultyId(String facultyId);

    /**
     * Delete evaluations for a specific period (admin operation)
     */
    void deleteBySemesterAndSchoolYear(String semester, Integer schoolYear);
}

