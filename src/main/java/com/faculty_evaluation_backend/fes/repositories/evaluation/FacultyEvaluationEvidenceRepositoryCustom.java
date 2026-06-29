package com.faculty_evaluation_backend.fes.repositories.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationEvidence;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.EvaluationEvidenceCriterion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface FacultyEvaluationEvidenceRepositoryCustom {

    Page<FacultyEvaluationEvidence> findByFilters(
            String facultyId,
            String classCode,
            String subjectCode,
            String semester,
            Integer schoolYear,
            EvaluationEvidenceCriterion criterion,
            Pageable pageable
    );

    Slice<FacultyEvaluationEvidence> findSliceByFilters(
            String facultyId,
            String classCode,
            String subjectCode,
            String semester,
            Integer schoolYear,
            EvaluationEvidenceCriterion criterion,
            Pageable pageable
    );
}
