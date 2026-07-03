package com.faculty_evaluation_backend.fes.services.data.evaluation;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationPrintResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDetailsDTO;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.evaluation.CommitmentAndTransparency;
import com.faculty_evaluation_backend.fes.entities.evaluation.ContentKnowledgePedagogyAndTechnology;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.evaluation.ManagementOfTeachingAndLearning;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.RatingScale;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySubjectRepository;
import com.faculty_evaluation_backend.fes.services.data.evaluation.strategies.EvaluationStrategy;
import com.faculty_evaluation_backend.fes.services.data.evaluation.strategies.EvaluationStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationDataService {
    private final EvaluationStrategyFactory factory;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final PrimarySubjectRepository primarySubjectRepository;
    private final PrimaryStudentRepository primaryStudentRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;

    @Transactional(transactionManager = "primaryTransactionManager")
    @Caching(evict = {
            @CacheEvict(value = "evaluatedCount", key = "#baseEvaluationDTO.evaluatorId"),
            @CacheEvict(value = "activeAccessCode", key = "#baseEvaluationDTO.evaluatorId"),
            @CacheEvict(value = "adminDashboard", allEntries = true),
            @CacheEvict(value = "adminDashboardSummary", allEntries = true),
            @CacheEvict(value = "adminDashboardPrograms", allEntries = true),
            @CacheEvict(value = "adminDashboardFacultyLoads", allEntries = true),
            @CacheEvict(value = "facultyEvaluationReports", allEntries = true),
            @CacheEvict(value = "studentSections", allEntries = true),
            @CacheEvict(value = "studentEvaluationStatus", allEntries = true),
            @CacheEvict(value = "studentFacultyEvaluations", allEntries = true),
            @CacheEvict(value = "facultyEvaluationScores", allEntries = true),
            @CacheEvict(value = "studentFacultyClassEvaluationChecks", allEntries = true),
            @CacheEvict(value = "supervisorEvaluatedStudents", allEntries = true)
    })
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

    @Cacheable(value = "facultyEvaluationReports", key = "#facultyId")
    public List<FacultyEvaluationPrintResponse> getSumOfAllFacultyEvaluationPerSubject(String facultyId) {

        SchoolYearAndSemester schoolYearAndSemester = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).orElseThrow(() -> new RuntimeException("No active school year and semester found."));

        List<FacultyClassDetailsDTO> facultyClassDetails = facultyEvaluationScoreRepository.findDistinctClassDetailsByFacultyIdAndSchoolYearAndSemester(facultyId, schoolYearAndSemester.getSchoolYear(), schoolYearAndSemester.getSemester().getValue());

        if (facultyClassDetails.isEmpty()) {
            return List.of();
        }

        List<FacultyEvaluationScore> allEvaluations =
                facultyEvaluationScoreRepository
                        .findByFacultyIdAndSchoolYearAndSemesterWithFaculty(
                                facultyId,
                                schoolYearAndSemester.getSchoolYear(),
                                schoolYearAndSemester.getSemester().getValue()
                        );

        if (allEvaluations.isEmpty()) {
            return List.of();
        }

        Map<String, List<FacultyEvaluationScore>> evaluationsByClass =
                allEvaluations.stream()
                        .collect(
                                Collectors.groupingBy(
                                        FacultyEvaluationScore::getClassCode
                                )
                        );

        Set<String> evaluatorIds =
                allEvaluations.stream()
                        .map(FacultyEvaluationScore::getEvaluatorId)
                        .collect(Collectors.toSet());

        Set<String> studentEvaluatorIds =
                evaluatorIds.isEmpty()
                        ? Set.of()
                        : primaryStudentRepository
                                .findExistingStudentIds(evaluatorIds);

        List<FacultyEvaluationPrintResponse> responses = new ArrayList<>();

        for (FacultyClassDetailsDTO facultyClassDetail : facultyClassDetails) {

            List<FacultyEvaluationScore> evaluations =
                    evaluationsByClass.getOrDefault(
                            facultyClassDetail.getClassCode(),
                            List.of()
                    );
            log.debug("""
                            Evaluations Found
                            Faculty ID: {}
                            Class Code: {}
                            School Year: {}
                            Semester: {}
                            Total Evaluations: {}
                            """, facultyId, facultyClassDetail.getClassCode(), schoolYearAndSemester.getSchoolYear(), schoolYearAndSemester.getSemester().getValue(),

                    evaluations.size());
            if (evaluations.isEmpty()) {
                continue;
            }
            List<FacultyEvaluationScore> studentEvaluations = new ArrayList<>();
            List<FacultyEvaluationScore> supervisorEvaluations = new ArrayList<>();

            for (FacultyEvaluationScore evaluation : evaluations) {

                boolean isStudent =
                        studentEvaluatorIds.contains(
                                evaluation.getEvaluatorId()
                        );

                if (isStudent) {

                    studentEvaluations.add(evaluation);

                } else {

                    supervisorEvaluations.add(evaluation);
                }
            }
            double setRating = calculateAverage(studentEvaluations);
            double sefRating = calculateAverage(supervisorEvaluations);
            FacultyEvaluationScore first = evaluations.getFirst();
            String studentComments = studentEvaluations.stream()

                    .map(FacultyEvaluationScore::getCommentsOrFeedbacks)

                    .filter(comment -> comment != null && !comment.isBlank())

                    .distinct()

                    .reduce((a, b) -> a + "\n• " + b)

                    .orElse("-");
            String supervisorComments = supervisorEvaluations.stream()

                    .map(FacultyEvaluationScore::getCommentsOrFeedbacks)

                    .filter(comment -> comment != null && !comment.isBlank())

                    .distinct()

                    .reduce((a, b) -> a + "\n• " + b)

                    .orElse("-");

            String evaluatorType;

            boolean isStudent =
                    studentEvaluatorIds.contains(first.getEvaluatorId());

            if (isStudent) {

                evaluatorType = "SET";

            } else {

                evaluatorType = "SEF";
            }
            FacultyEvaluationPrintResponse response = FacultyEvaluationPrintResponse.builder()

                    .facultyEvaluationScoreId(first.getFacultyEvaluationScoreId())

                    .facultyId(first.getFacultyId())

                    .facultyName(first.getFaculty().getFirstname() + " " + first.getFaculty().getMiddlename() + " " + first.getFaculty().getLastname())

                    .evaluatorId(first.getEvaluatorId())

                    .evaluatorType(evaluatorType)

                    .college(String.valueOf(first.getFaculty().getCollege()))

                    .classCode(facultyClassDetail.getClassCode()).sectionCode(facultyClassDetail.getSectionCode()).programCode(facultyClassDetail.getProgramCode()).position(first.getFaculty().getPosition())

                    .semester(first.getSemester())

                    .schoolYear(first.getSchoolYear())

                    .subjectCode(first.getSubjectCode())

                    .yearLevel(facultyClassDetail.getYearLevel())

                    .studentComments(studentComments)

                    .supervisorComments(supervisorComments)

                    .overallAverageScore(setRating)

                    .overallInterpretation(determineInterpretation(setRating))

                    .numberOfStudents(studentEvaluations.size())

                    .setRating(setRating)

                    .sefRating(sefRating)

                    .build();

            responses.add(response);
        }

        return responses;
    }

    @Cacheable(value = "classStudentCounts", key = "#classCode")
    public Integer numberOfStudents(String classCode) {
        return primaryStudentLoadRepository.findTotalStudentsInClass(classCode);
    }

    private double calculateAverage(List<FacultyEvaluationScore> evaluations) {

        if (evaluations.isEmpty()) {
            return 0.0;
        }

        double total = evaluations.stream().mapToDouble(FacultyEvaluationScore::getOverallAverageScore).sum();

        double average = total / evaluations.size();

        return Math.round(average * 100.0) / 100.0;
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

        FacultyEvaluationScore evaluation = FacultyEvaluationScore.builder().facultyId(dto.getFacultyId()).evaluatorId(dto.getEvaluatorId()).evaluationType(dto.getEvaluationType()).classCode(dto.getClassCode()).subjectCode(dto.getSubjectCode()).semester(dto.getSemester()).yearLevel(dto.getYearLevel()).schoolYear(dto.getSchoolYear()).managementOfTeachingAndLearning(teaching).contentKnowledgePedagogyAndTechnology(content).commitmentAndTransparency(commitment).commentsOrFeedbacks(dto.getCommentsOrFeedbacks()).build();

        evaluation.calculateOverallScore();

        return evaluation;
    }

}
