package com.faculty_evaluation_backend.fes.services.data.evaluation;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationPrintResponse;
import com.faculty_evaluation_backend.fes.entities.evaluation.CommitmentAndTransparency;
import com.faculty_evaluation_backend.fes.entities.evaluation.ContentKnowledgePedagogyAndTechnology;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.evaluation.ManagementOfTeachingAndLearning;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.RatingScale;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySubjectRepository;
import com.faculty_evaluation_backend.fes.services.data.evaluation.strategies.EvaluationStrategy;
import com.faculty_evaluation_backend.fes.services.data.evaluation.strategies.EvaluationStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationDataService {
    private final EvaluationStrategyFactory factory;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final PrimarySubjectRepository primarySubjectRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;

    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyEvaluationScore submit(BaseEvaluationDTO baseEvaluationDTO) {
        log.info("Submitting {} evaluation", baseEvaluationDTO.getEvaluationType());
        primaryFacultyRepository.findByFacultyId(baseEvaluationDTO.getFacultyId()).orElseThrow(() -> new RuntimeException("Faculty not found"));
        primarySubjectRepository.findBySubjectCode(baseEvaluationDTO.getSubjectCode()).orElseThrow(() -> new RuntimeException("Subject not found"));
        EvaluationStrategy evaluationStrategy = factory.getStrategy(baseEvaluationDTO.getEvaluationType());
        evaluationStrategy.validate(baseEvaluationDTO);
        if (evaluationStrategy.isDuplicate(baseEvaluationDTO)) {
            throw new RuntimeException("Duplicate Evaluation");
        }
        FacultyEvaluationScore evaluationScore = buildEvaluation(baseEvaluationDTO);
        if (!evaluationScore.isComplete()) {
            throw new RuntimeException("Incomplete Evaluation");
        }
        return facultyEvaluationScoreRepository.save(evaluationScore);
    }

    public List<FacultyEvaluationPrintResponse> getFacultyEvaluationScoresByFacultyId(String facultyId) {
        return facultyEvaluationScoreRepository.findByFacultyId(facultyId).stream()
                .map(score -> FacultyEvaluationPrintResponse.builder()
                        .facultyEvaluationScoreId(score.getFacultyEvaluationScoreId())
                        .facultyId(score.getFacultyId())
                        .facultyName(score.getFaculty().getFirstname() + " " + score.getFaculty().getMiddlename() + " " + score.getFaculty().getLastname())
                        .evaluatorId(score.getEvaluatorId())
                        .college(String.valueOf(score.getFaculty().getCollege()))
                        .classCode(score.getClassCode())
                        .position(score.getFaculty().getPosition())
                        .semester(score.getSemester())
                        .schoolYear(score.getSchoolYear())
                        .subjectCode(score.getSubjectCode())
                        .yearLevel(score.getYearLevel())
                        .comments(score.getCommentsOrFeedbacks())
                        .overallAverageScore(score.getOverallAverageScore())
                        .overallInterpretation(score.getOverallInterpretation())
                        .build()).toList();
    }

    public List<FacultyEvaluationPrintResponse> getAllFacultyEvaluationPerSubject(String facultyId, String classCode) {
        List<FacultyEvaluationScore> evaluations = facultyEvaluationScoreRepository.findByFacultyIdAndClassCode(facultyId, classCode);
        if (evaluations.isEmpty()) {
            return List.of();
        }
        double totalScore = evaluations.stream().mapToDouble(FacultyEvaluationScore::getOverallAverageScore).sum();
        int totalEvaluations = evaluations.size();
        double averageScore = totalScore / totalEvaluations;
        averageScore = Math.round(averageScore * 100.0) / 100.0;
        FacultyEvaluationScore first = evaluations.getFirst();
        String combinedComments = evaluations.stream().map(FacultyEvaluationScore::getCommentsOrFeedbacks).filter(comment -> comment != null && !comment.isBlank()).distinct().reduce((a, b) -> a + "\n• " + b).orElse("-");
        FacultyEvaluationPrintResponse response = FacultyEvaluationPrintResponse.builder()
                .facultyEvaluationScoreId(first.getFacultyEvaluationScoreId())
                .facultyId(first.getFacultyId())
                .facultyName(first.getFaculty().getFirstname() + " " + first.getFaculty().getMiddlename() + " " + first.getFaculty().getLastname())
                .evaluatorId(first.getEvaluatorId())
                .college(String.valueOf(first.getFaculty().getCollege()))
                .classCode(first.getClassCode())
                .position(first.getFaculty().getPosition())
                .semester(first.getSemester())
                .schoolYear(first.getSchoolYear())
                .subjectCode(first.getSubjectCode())
                .yearLevel(first.getYearLevel())
                .comments(combinedComments)
                .overallAverageScore(averageScore)
                .overallInterpretation(determineInterpretation(averageScore))
                .numberOfStudents(totalEvaluations)
                .build();
        return List.of(response);
    }

    public Integer numberOfStudents(String classCode) {
        return primaryStudentLoadRepository.findTotalStudentsInClass(classCode);
    }

    private String determineInterpretation(double rating) {

        if (rating >= 90) {
            return "Excellent";
        }

        if (rating >= 80) {
            return "Very Good";
        }

        if (rating >= 70) {
            return "Good";
        }

        if (rating >= 60) {
            return "Fair";
        }

        return "Poor";
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

        ManagementOfTeachingAndLearning teaching = ManagementOfTeachingAndLearning.builder().facultyId(dto.getFacultyId()).punctuality(map(r, "punctuality")).courseClarity(map(r, "courseClarity")).timeManagement(map(r, "timeManagement")).criticalThinkingFacilitation(map(r, "criticalThinkingFacilitation")).independentLearningGuidance(map(r, "independentLearningGuidance")).feedbackCommunication(map(r, "feedbackCommunication")).build();

        ContentKnowledgePedagogyAndTechnology content = ContentKnowledgePedagogyAndTechnology.builder().facultyId(dto.getFacultyId()).subjectKnowledge(map(r, "subjectKnowledge")).contentSimplification(map(r, "contentSimplification")).realWorldApplication(map(r, "realWorldApplication")).technologyIntegration(map(r, "technologyIntegration")).assessmentAlignment(map(r, "assessmentAlignment")).build();

        CommitmentAndTransparency commitment = CommitmentAndTransparency.builder().facultyId(dto.getFacultyId()).diversityRecognition(map(r, "diversityRecognition")).consultationSupport(map(r, "consultationSupport")).immediateFeedback(map(r, "immediateFeedback")).transparentGrading(map(r, "transparentGrading")).build();

        FacultyEvaluationScore evaluation = FacultyEvaluationScore.builder().facultyId(dto.getFacultyId()).evaluatorId(dto.getEvaluatorId()).classCode(dto.getClassCode()).subjectCode(dto.getSubjectCode()).semester(dto.getSemester()).yearLevel(dto.getYearLevel()).schoolYear(dto.getSchoolYear()).managementOfTeachingAndLearning(teaching).contentKnowledgePedagogyAndTechnology(content).commitmentAndTransparency(commitment).commentsOrFeedbacks(dto.getCommentsOrFeedbacks()).build();

        evaluation.calculateOverallScore();

        return evaluation;
    }


}
