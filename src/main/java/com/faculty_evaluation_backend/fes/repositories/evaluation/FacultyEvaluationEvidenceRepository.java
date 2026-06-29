package com.faculty_evaluation_backend.fes.repositories.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationEvidence;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.EvaluationEvidenceCriterion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyEvaluationEvidenceRepository
        extends JpaRepository<FacultyEvaluationEvidence, Long> {

    @Query("""
                SELECT e
                FROM FacultyEvaluationEvidence e
                WHERE e.facultyId = :facultyId
                  AND (:classCode IS NULL OR e.classCode = :classCode)
                  AND (:subjectCode IS NULL OR e.subjectCode = :subjectCode)
                  AND (:semester IS NULL OR e.semester = :semester)
                  AND (:schoolYear IS NULL OR e.schoolYear = :schoolYear)
                  AND (:criterion IS NULL OR e.criterion = :criterion)
            """)
    Page<FacultyEvaluationEvidence> findByFilters(
            @Param("facultyId") String facultyId,
            @Param("classCode") String classCode,
            @Param("subjectCode") String subjectCode,
            @Param("semester") String semester,
            @Param("schoolYear") Integer schoolYear,
            @Param("criterion") EvaluationEvidenceCriterion criterion,
            Pageable pageable
    );
}
