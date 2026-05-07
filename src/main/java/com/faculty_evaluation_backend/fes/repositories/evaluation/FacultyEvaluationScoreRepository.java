package com.faculty_evaluation_backend.fes.repositories.evaluation;


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

}

