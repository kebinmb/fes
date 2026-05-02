package com.faculty_evaluation_backend.fes.services.data.students.evaluation;

import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentEvaluationService {
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public boolean hasEvaluatedFacultyForClass(
            String facultyId,
            String evaluatorId,
            String classCode,
            String subjectCode,
            String yearLevel,
            String semester,
            Integer schoolYear
    ){
        if (isBlank(facultyId) || isBlank(evaluatorId)) {
            throw new BadRequestException("Faculty ID and Evaluator ID are required");
        }

        if (isBlank(classCode) || isBlank(semester) || schoolYear == null) {
            throw new BadRequestException("ClassCode, Semester, and SchoolYear are required");
        }

        try {
            return facultyEvaluationScoreRepository
                    .existsByFacultyIdAndEvaluatorIdAndClassCodeAndSubjectCodeAndYearLevelAndSemesterAndSchoolYear(
                            facultyId.trim(),
                            evaluatorId.trim(),
                            classCode.trim(),
                            subjectCode.trim(),
                            yearLevel.trim(),
                            semester.trim(),
                            schoolYear
                    );
        } catch (Exception ex) {
            throw new RuntimeException("Unable to check evaluation status", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
