package com.faculty_evaluation_backend.fes.repositories.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyEvaluationEvidenceRepository
        extends JpaRepository<FacultyEvaluationEvidence, Long>,
        FacultyEvaluationEvidenceRepositoryCustom {
}
