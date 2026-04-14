package com.faculty_evaluation_backend.fes.services.data.evaluation;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.entities.evaluation.CommitmentAndTransparency;
import com.faculty_evaluation_backend.fes.entities.evaluation.ContentKnowledgePedagogyAndTechnology;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.evaluation.ManagementOfTeachingAndLearning;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.RatingScale;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySubjectRepository;
import com.faculty_evaluation_backend.fes.services.data.evaluation.strategies.EvaluationStrategy;
import com.faculty_evaluation_backend.fes.services.data.evaluation.strategies.EvaluationStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationDataService {
    private final EvaluationStrategyFactory factory;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final PrimaryFacultyRepository  primaryFacultyRepository;
    private final PrimarySubjectRepository  primarySubjectRepository;

    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyEvaluationScore submit(BaseEvaluationDTO baseEvaluationDTO){
        log.info("Submitting {} evaluation", baseEvaluationDTO.getEvaluationType());

        primaryFacultyRepository.findByFacultyId(baseEvaluationDTO.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        primarySubjectRepository.findBySubjectCode(baseEvaluationDTO.getSubjectCode())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        EvaluationStrategy evaluationStrategy = factory.getStrategy(baseEvaluationDTO.getEvaluationType());

        evaluationStrategy.validate(baseEvaluationDTO);

        if(evaluationStrategy.isDuplicate(baseEvaluationDTO)){
            throw new RuntimeException("Duplicate Evaluation");
        }

        FacultyEvaluationScore evaluationScore = buildEvaluation(baseEvaluationDTO);

        if (!evaluationScore.isComplete()){
            throw new RuntimeException("Incomplete Evaluation");
        }

        return  facultyEvaluationScoreRepository.save(evaluationScore);
    }
    private RatingScale map(Map<String, String> ratings, String key) {
        String value = ratings.get(key);
        if (value == null) {
            throw new RuntimeException("Missing rating for: " + key);
        }
        return RatingScale.fromCode(value);
    }
    private FacultyEvaluationScore buildEvaluation(BaseEvaluationDTO dto) {

        Map<String, String> r = dto.getRatings();

        ManagementOfTeachingAndLearning teaching =
                ManagementOfTeachingAndLearning.builder()
                        .facultyId(dto.getFacultyId())
                        .punctuality(map(r, "punctuality"))
                        .courseClarity(map(r, "courseClarity"))
                        .timeManagement(map(r, "timeManagement"))
                        .criticalThinkingFacilitation(map(r, "criticalThinkingFacilitation"))
                        .independentLearningGuidance(map(r, "independentLearningGuidance"))
                        .feedbackCommunication(map(r, "feedbackCommunication"))
                        .build();

        ContentKnowledgePedagogyAndTechnology content =
                ContentKnowledgePedagogyAndTechnology.builder()
                        .facultyId(dto.getFacultyId())
                        .subjectKnowledge(map(r, "subjectKnowledge"))
                        .contentSimplification(map(r, "contentSimplification"))
                        .realWorldApplication(map(r, "realWorldApplication"))
                        .technologyIntegration(map(r, "technologyIntegration"))
                        .assessmentAlignment(map(r, "assessmentAlignment"))
                        .build();

        CommitmentAndTransparency commitment =
                CommitmentAndTransparency.builder()
                        .facultyId(dto.getFacultyId())
                        .diversityRecognition(map(r, "diversityRecognition"))
                        .consultationSupport(map(r, "consultationSupport"))
                        .immediateFeedback(map(r, "immediateFeedback"))
                        .transparentGrading(map(r, "transparentGrading"))
                        .build();

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

        evaluation.calculateOverallScore();

        return evaluation;
    }
}
