package com.faculty_evaluation_backend.fes.services.data.evaluation;

import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationDataService {
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyEvaluationScore submitEvaluationForSubject(SubjectEvaluationDTO dto) {
        log.info("Submitting evaluation for faculty: {} by student: {} for subject: {}",
                dto.getFacultyId(), dto.getEvaluatorId(), dto.getSubjectCode());

        validateEvaluationContext(dto);

        if (facultyEvaluationScoreRepository.existsByFacultyIdAndEvaluatorIdAndClassCodeAndSemesterAndSchoolYear(
                dto.getFacultyId(), dto.getEvaluatorId(), dto.getClassCode(),
                dto.getSemester(), dto.getSchoolYear())) {
            log.warn("Duplicate evaluation detected - Faculty: {}, Student: {}, Class: {}",
                    dto.getFacultyId(), dto.getEvaluatorId(), dto.getClassCode());
            throw new DuplicateEvaluationException(
                    "You have already evaluated this faculty for this subject in " +
                            dto.getSemester() + " semester " + dto.getSchoolYear()
            );
        }


        ManagementOfTeachingAndLearning teaching = buildTeachingEvaluation(dto);
        ContentKnowledgePedagogyAndTechnology content = buildContentEvaluation(dto);
        CommitmentAndTransparency commitment = buildCommitmentEvaluation(dto);

        FacultyEvaluationScore evaluation = FacultyEvaluationScore.builder()
                .facultyId(dto.getFacultyId())
                .evaluatorId(dto.getEvaluatorId())
                .classCode(dto.getClassCode())
                .subjectCode(dto.getSubjectCode())
                .semester(dto.getSemester())
                .schoolYear(dto.getSchoolYear())
                .managementOfTeachingAndLearning(teaching)
                .contentKnowledgePedagogyAndTechnology(content)
                .commitmentAndTransparency(commitment)
                .commentsOrFeedbacks(dto.getCommentsOrFeedbacks())
                .build();

        StudentAccessCode code = studentAccessCodeRepository
                .findByStudentIdAndAccessCode(dto.evaluatorId, dto.accessCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID or access code"));

        if (!evaluation.isComplete()) {
            log.error("Incomplete evaluation submission for faculty: {}", dto.getFacultyId());
            throw new IncompleteEvaluationException("All 15 criteria must be rated");
        }

        FacultyEvaluationScore saved = facultyEvaluationScoreRepository.save(evaluation);

        log.info("✅ Evaluation submitted - ID: {}, Overall Score: {}, Interpretation: {}",
                saved.getFacultyEvaluationScoreId(),
                saved.getOverallAverageScore(),
                saved.getOverallInterpretation());
        log.info("📊 Breakdown: {}", saved.getScoreBreakdown());

        return saved;
    }

}
