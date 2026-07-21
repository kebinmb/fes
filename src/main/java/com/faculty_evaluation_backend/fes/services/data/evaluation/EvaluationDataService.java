package com.faculty_evaluation_backend.fes.services.data.evaluation;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationBulkReportResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationPrintResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationGeneratedReportResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationReportVerificationResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDetailsDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDetailsProjection;
import com.faculty_evaluation_backend.fes.entities.authentication.CustomUserDetails;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.evaluation.CommitmentAndTransparency;
import com.faculty_evaluation_backend.fes.entities.evaluation.ContentKnowledgePedagogyAndTechnology;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationReport;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.evaluation.ManagementOfTeachingAndLearning;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationReportStatus;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.RatingScale;
import com.faculty_evaluation_backend.fes.entities.primary.FacultyWorkload;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationReportRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.FacultyWorkloadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySubjectRepository;
import com.faculty_evaluation_backend.fes.services.data.evaluation.strategies.EvaluationStrategy;
import com.faculty_evaluation_backend.fes.services.data.evaluation.strategies.EvaluationStrategyFactory;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.utilities.normalization.SemesterNormalizer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationDataService {
    private static final Set<String> ALLOWED_LEGACY_DATABASES = Set.of(
            "LEGACY_TALISAY",
            "LEGACY_ALIJIS",
            "LEGACY_FT",
            "LEGACY_BINALBAGAN"
    );

    private final EvaluationStrategyFactory factory;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final PrimarySubjectRepository primarySubjectRepository;
    private final PrimaryStudentRepository primaryStudentRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    private final FacultyWorkloadRepository facultyWorkloadRepository;
    private final FacultyEvaluationReportRepository facultyEvaluationReportRepository;
    private final UserAccountsRepository userAccountsRepository;
    private final ObjectMapper objectMapper;

    @Transactional(transactionManager = "primaryTransactionManager")
    @Caching(evict = {
            @CacheEvict(value = "evaluatedCount", key = "#baseEvaluationDTO.evaluatorId"),
            @CacheEvict(value = "activeAccessCode", key = "#baseEvaluationDTO.evaluatorId"),
            @CacheEvict(value = "adminDashboard", allEntries = true),
            @CacheEvict(value = "adminDashboardSummary", allEntries = true),
            @CacheEvict(value = "adminDashboardPrograms", allEntries = true),
            @CacheEvict(value = "adminDashboardFacultyLoads", allEntries = true),
            @CacheEvict(value = "facultyEvaluationReports", key = "#baseEvaluationDTO.facultyId"),
            @CacheEvict(value = "studentSections", allEntries = true),
            @CacheEvict(value = "studentEvaluationStatus", allEntries = true),
            @CacheEvict(value = "studentFacultyEvaluations", allEntries = true),
            @CacheEvict(value = "facultyEvaluationScores", allEntries = true),
            @CacheEvict(value = "facultyEvaluationReadiness", allEntries = true),
            @CacheEvict(value = "facultyEvaluationReadinessFacultyIds", allEntries = true),
            @CacheEvict(value = "studentFacultyClassEvaluationChecks", allEntries = true),
            @CacheEvict(value = "supervisorEvaluatedStudents", allEntries = true)
    })
    public FacultyEvaluationScore submit(BaseEvaluationDTO baseEvaluationDTO) {
        normalizeEvaluationTerm(baseEvaluationDTO);
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

    private void normalizeEvaluationTerm(BaseEvaluationDTO baseEvaluationDTO) {
        if (baseEvaluationDTO == null) {
            return;
        }

        baseEvaluationDTO.setSemester(
                SemesterNormalizer.toCanonicalValue(baseEvaluationDTO.getSemester())
        );
    }

    @Cacheable(value = "facultyEvaluationReports", key = "#facultyId")
    public List<FacultyEvaluationPrintResponse> getSumOfAllFacultyEvaluationPerSubject(String facultyId) {

        SchoolYearAndSemester schoolYearAndSemester = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).orElseThrow(() -> new RuntimeException("No active school year and semester found."));

        List<FacultyClassDetailsDTO> facultyClassDetails =
                facultyEvaluationScoreRepository
                        .findDistinctClassDetailsByFacultyIdAndSchoolYearAndSemester(
                                facultyId,
                                schoolYearAndSemester.getSchoolYear(),
                                schoolYearAndSemester.getSemester().getValue()
                        )
                        .stream()
                        .map(this::toFacultyClassDetailsDTO)
                        .toList();

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

        Set<String> supervisorEvaluatorIds =
                evaluatorIds.stream()
                        .filter(evaluatorId ->
                                !studentEvaluatorIds.contains(evaluatorId)
                        )
                        .collect(Collectors.toSet());

        Map<String, PrimaryFaculty> supervisorsByFacultyId =
                supervisorEvaluatorIds.isEmpty()
                        ? Map.of()
                        : primaryFacultyRepository
                                .findByFacultyIdIn(supervisorEvaluatorIds)
                                .stream()
                                .collect(Collectors.toMap(
                                        PrimaryFaculty::getFacultyId,
                                        supervisor -> supervisor,
                                        (current, candidate) -> current
                                ));

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

            PrimaryFaculty supervisor =
                    supervisorEvaluations.stream()
                            .map(FacultyEvaluationScore::getEvaluatorId)
                            .map(supervisorsByFacultyId::get)
                            .filter(item -> item != null)
                            .findFirst()
                            .orElse(null);
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

                    .supervisorName(supervisor == null
                            ? null
                            : facultyDisplayName(supervisor))

                    .supervisorDesignation(supervisor == null
                            ? null
                            : supervisor.getPosition())

                    .overallAverageScore(setRating)

                    .overallInterpretation(determineInterpretation(setRating))

                    .numberOfStudents(studentEvaluations.size())

                    .setRating(setRating)

                    .sefRating(sefRating)

                    .build();

            responses.add(response);
        }

        return selectPrintableWorkloadSubjects(
                facultyId,
                schoolYearAndSemester.getSchoolYear(),
                schoolYearAndSemester.getSemester().getValue(),
                responses
        );
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyEvaluationGeneratedReportResponse generateFacultyEvaluationReport(
            String facultyId,
            String verificationBaseUrl,
            CustomUserDetails generatedBy
    ) {
        SchoolYearAndSemester term =
                schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active school year and semester found."
                                )
                        );
        List<FacultyEvaluationPrintResponse> items =
                getSumOfAllFacultyEvaluationPerSubject(facultyId);

        if (items.isEmpty()) {
            throw new BadRequestException(
                    "No printable evaluated workload subjects found for this faculty."
            );
        }

        facultyEvaluationReportRepository
                .findByFacultyIdAndSchoolYearAndSemesterAndStatus(
                        facultyId,
                        term.getSchoolYear(),
                        term.getSemester().getValue(),
                        FacultyEvaluationReportStatus.VALID
                )
                .forEach(report ->
                        report.setStatus(
                                FacultyEvaluationReportStatus.SUPERSEDED
                        )
                );

        String reportId = UUID.randomUUID().toString();
        String verificationUrl =
                normalizeBaseUrl(verificationBaseUrl)
                        + "/verify-report/"
                        + reportId;
        int versionNumber =
                (int) facultyEvaluationReportRepository
                        .countByFacultyIdAndSchoolYearAndSemester(
                                facultyId,
                                term.getSchoolYear(),
                                term.getSemester().getValue()
                        )
                        + 1;
        Instant generatedAt = Instant.now();
        String facultyName = items.getFirst().getFacultyName();
        Long generatedByUserId =
                resolveGeneratedByUserId(generatedBy);
        String generatedByUsername =
                preparedByName(generatedByUserId, generatedBy);
        Map<String, Object> snapshot =
                reportSnapshot(
                        reportId,
                        facultyId,
                        facultyName,
                        term.getSchoolYear(),
                        term.getSemester().getValue(),
                        versionNumber,
                        generatedAt,
                        generatedByUserId,
                        generatedByUsername,
                        items
                );
        String snapshotJson = toJson(snapshot);
        String reportHash = sha256(snapshotJson);

        FacultyEvaluationReport report =
                FacultyEvaluationReport.builder()
                        .reportId(reportId)
                        .facultyId(facultyId)
                        .facultyName(facultyName)
                        .schoolYear(term.getSchoolYear())
                        .semester(term.getSemester().getValue())
                        .versionNumber(versionNumber)
                        .status(FacultyEvaluationReportStatus.VALID)
                        .reportHash(reportHash)
                        .snapshotJson(snapshotJson)
                        .verificationUrl(verificationUrl)
                        .generatedByUserId(generatedByUserId)
                        .generatedByUsername(generatedByUsername)
                        .generatedAt(generatedAt)
                        .build();

        FacultyEvaluationReport savedReport =
                facultyEvaluationReportRepository.save(report);

        return toGeneratedReportResponse(
                savedReport,
                items,
                qrCodeDataUri(verificationUrl)
        );
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyEvaluationBulkReportResponse generateFacultyEvaluationReports(
            String legacyDatabase,
            College college,
            String verificationBaseUrl,
            CustomUserDetails generatedBy
    ) {
        String normalizedLegacyDatabase =
                normalizeLegacyDatabase(legacyDatabase);
        List<String> facultyIds =
                primaryFacultyRepository.findPrintableFacultyIds(
                        College.FOR_MIGRATION,
                        normalizedLegacyDatabase,
                        college
                );

        if (facultyIds.isEmpty()) {
            throw new BadRequestException(
                    "No faculty records found for the selected filters."
            );
        }

        List<FacultyEvaluationGeneratedReportResponse> reports =
                new ArrayList<>();
        List<String> skippedFacultyIds = new ArrayList<>();

        for (String facultyId : facultyIds) {
            try {
                reports.add(
                        generateFacultyEvaluationReport(
                                facultyId,
                                verificationBaseUrl,
                                generatedBy
                        )
                );
            } catch (BadRequestException ex) {
                skippedFacultyIds.add(facultyId);
            }
        }

        if (reports.isEmpty()) {
            throw new BadRequestException(
                    "No printable evaluated faculty reports found for the selected filters."
            );
        }

        return FacultyEvaluationBulkReportResponse.builder()
                .requestedCount(facultyIds.size())
                .generatedCount(reports.size())
                .skippedFacultyIds(skippedFacultyIds)
                .reports(reports)
                .build();
    }


    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyEvaluationBulkReportResponse
    generateFacultyEvaluationReportsForFacultyIds(
            List<String> facultyIds,
            String verificationBaseUrl,
            CustomUserDetails generatedBy
    ) {
        if (facultyIds == null || facultyIds.isEmpty()) {
            throw new BadRequestException(
                    "No print-ready faculty records found for the selected filters."
            );
        }

        List<FacultyEvaluationGeneratedReportResponse> reports =
                new ArrayList<>();
        List<String> skippedFacultyIds = new ArrayList<>();

        for (String facultyId : facultyIds) {
            try {
                reports.add(
                        generateFacultyEvaluationReport(
                                facultyId,
                                verificationBaseUrl,
                                generatedBy
                        )
                );
            } catch (BadRequestException ex) {
                skippedFacultyIds.add(facultyId);
            }
        }

        if (reports.isEmpty()) {
            throw new BadRequestException(
                    "No printable evaluated faculty reports found for the selected filters."
            );
        }

        return FacultyEvaluationBulkReportResponse.builder()
                .requestedCount(facultyIds.size())
                .generatedCount(reports.size())
                .skippedFacultyIds(skippedFacultyIds)
                .reports(reports)
                .build();
    }
    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public FacultyEvaluationReportVerificationResponse verifyReport(
            String reportId
    ) {
        FacultyEvaluationReport report =
                facultyEvaluationReportRepository.findById(reportId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Faculty evaluation report was not found."
                                )
                        );

        return FacultyEvaluationReportVerificationResponse.builder()
                .reportId(report.getReportId())
                .facultyId(report.getFacultyId())
                .facultyName(report.getFacultyName())
                .schoolYear(report.getSchoolYear())
                .semester(report.getSemester())
                .versionNumber(report.getVersionNumber())
                .status(report.getStatus())
                .reportHash(report.getReportHash())
                .generatedAt(report.getGeneratedAt())
                .generatedByUsername(report.getGeneratedByUsername())
                .message(report.getStatus() == FacultyEvaluationReportStatus.VALID
                        ? "This report is valid and matches an official generated record."
                        : "This report exists but is no longer the current valid version.")
                .build();
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

    private FacultyClassDetailsDTO toFacultyClassDetailsDTO(
            FacultyClassDetailsProjection row
    ) {
        return new FacultyClassDetailsDTO(
                row.getClassCode(),
                row.getSectionCode(),
                row.getProgramCode(),
                row.getYearLevel()
        );
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

    private List<FacultyEvaluationPrintResponse> selectPrintableWorkloadSubjects(
            String facultyId,
            Integer schoolYear,
            String semester,
            List<FacultyEvaluationPrintResponse> responses
    ) {
        if (responses.isEmpty()) {
            return responses;
        }

        List<FacultyWorkload> workloads =
                facultyWorkloadRepository.findAllByFacultyIdAndSchoolYearAndEquivalentSemester(
                        facultyId,
                        schoolYear,
                        semester
                );

        if (workloads.isEmpty()) {
            return responses;
        }

        List<FacultyWorkload> regularWorkloads =
                workloads.stream()
                        .filter(this::isRegularWorkload)
                        .toList();

        if (regularWorkloads.isEmpty()) {
            throw new BadRequestException(
                    "Faculty only has overload subjects, not allowed for printing"
            );
        }

        Map<String, FacultyWorkload> workloadsByClassCode =
                regularWorkloads.stream()
                        .filter(workload -> normalizeKey(workload.getClassCode()) != null)
                        .collect(Collectors.toMap(
                                workload -> normalizeKey(workload.getClassCode()),
                                workload -> workload,
                                this::preferWorkloadWithHours
                        ));

        Map<String, FacultyWorkload> workloadsByCourseSection =
                regularWorkloads.stream()
                        .collect(Collectors.toMap(
                                this::workloadCourseSectionKey,
                                workload -> workload,
                                this::preferWorkloadWithHours
                        ));

        return responses.stream()
                .filter(response ->
                        findMatchingWorkload(
                                response,
                                workloadsByClassCode,
                                workloadsByCourseSection
                        ).isPresent()
                )
                .toList();
    }

    private boolean isRegularWorkload(FacultyWorkload workload) {
        String loadStatus = normalizeKey(workload.getLoadStatus());

        return "REGULAR".equals(loadStatus)
                || "REGULAR_LOAD".equals(loadStatus);
    }

    private Optional<FacultyWorkload> findMatchingWorkload(
            FacultyEvaluationPrintResponse response,
            Map<String, FacultyWorkload> workloadsByClassCode,
            Map<String, FacultyWorkload> workloadsByCourseSection
    ) {
        String classCode = normalizeKey(response.getClassCode());

        if (classCode != null
                && workloadsByClassCode.containsKey(classCode)) {
            return Optional.of(workloadsByClassCode.get(classCode));
        }

        return Optional.ofNullable(
                workloadsByCourseSection.get(responseCourseSectionKey(response))
        );
    }

    private FacultyWorkload preferWorkloadWithHours(
            FacultyWorkload current,
            FacultyWorkload candidate
    ) {
        return safeBigDecimal(candidate.getTotalHoursPerWeek())
                .compareTo(safeBigDecimal(current.getTotalHoursPerWeek())) > 0
                ? candidate
                : current;
    }

    private String workloadCourseSectionKey(FacultyWorkload workload) {
        return keyParts(
                workload.getCourseCode(),
                workload.getProgramCode(),
                workload.getYearLevel(),
                workload.getSectionCode()
        );
    }

    private String responseCourseSectionKey(
            FacultyEvaluationPrintResponse response
    ) {
        return keyParts(
                response.getSubjectCode(),
                response.getProgramCode(),
                response.getYearLevel(),
                response.getSectionCode()
        );
    }

    private String keyParts(String... values) {
        return java.util.Arrays.stream(values)
                .map(this::normalizeKey)
                .map(value -> value == null ? "" : value)
                .collect(Collectors.joining("|"));
    }

    private String normalizeKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase();
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private FacultyEvaluationGeneratedReportResponse toGeneratedReportResponse(
            FacultyEvaluationReport report,
            List<FacultyEvaluationPrintResponse> items,
            String qrCodeDataUri
    ) {
        return FacultyEvaluationGeneratedReportResponse.builder()
                .reportId(report.getReportId())
                .facultyId(report.getFacultyId())
                .facultyName(report.getFacultyName())
                .schoolYear(report.getSchoolYear())
                .semester(report.getSemester())
                .versionNumber(report.getVersionNumber())
                .status(report.getStatus())
                .reportHash(report.getReportHash())
                .verificationUrl(report.getVerificationUrl())
                .qrCodeDataUri(qrCodeDataUri)
                .generatedByUserId(report.getGeneratedByUserId())
                .generatedByUsername(report.getGeneratedByUsername())
                .generatedAt(report.getGeneratedAt())
                .items(items)
                .build();
    }

    private Map<String, Object> reportSnapshot(
            String reportId,
            String facultyId,
            String facultyName,
            Integer schoolYear,
            String semester,
            Integer versionNumber,
            Instant generatedAt,
            Long generatedByUserId,
            String generatedByUsername,
            List<FacultyEvaluationPrintResponse> items
    ) {
        Map<String, Object> snapshot = new LinkedHashMap<>();

        snapshot.put("reportId", reportId);
        snapshot.put("facultyId", facultyId);
        snapshot.put("facultyName", facultyName);
        snapshot.put("schoolYear", schoolYear);
        snapshot.put("semester", semester);
        snapshot.put("versionNumber", versionNumber);
        snapshot.put("generatedAt", generatedAt.toString());
        snapshot.put("generatedByUserId", generatedByUserId);
        snapshot.put("generatedByUsername", generatedByUsername);
        snapshot.put("items", items);

        return snapshot;
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(
                    "Failed to create report verification snapshot.",
                    exception
            );
        }
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash =
                    digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);

            for (byte item : hash) {
                builder.append(String.format("%02x", item));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException(
                    "SHA-256 hashing is not available.",
                    exception
            );
        }
    }

    private String qrCodeDataUri(String value) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix =
                    writer.encode(value, BarcodeFormat.QR_CODE, 220, 220);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            MatrixToImageWriter.writeToStream(matrix, "PNG", output);

            return "data:image/png;base64,"
                    + Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (WriterException | java.io.IOException exception) {
            throw new RuntimeException("Failed to generate report QR code.", exception);
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            return "";
        }

        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }

    private String normalizeLegacyDatabase(String legacyDatabase) {
        if (legacyDatabase == null || legacyDatabase.trim().isEmpty()) {
            return null;
        }

        String normalized = legacyDatabase.trim().toUpperCase();

        if (!ALLOWED_LEGACY_DATABASES.contains(normalized)) {
            throw new BadRequestException(
                    "Invalid campus filter selected."
            );
        }

        return normalized;
    }

    private Long resolveGeneratedByUserId(CustomUserDetails generatedBy) {
        if (generatedBy != null) {
            return generatedBy.getUserId();
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails details) {
            return details.getUserId();
        }

        try {
            return Long.valueOf(String.valueOf(principal));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String preparedByName(
            Long generatedByUserId,
            CustomUserDetails generatedBy
    ) {
        if (generatedBy != null) {
            return preparedByName(generatedBy.getUser());
        }

        if (generatedByUserId == null) {
            return null;
        }

        return userAccountsRepository
                .findById(generatedByUserId)
                .map(this::preparedByName)
                .orElse(null);
    }

    private String preparedByName(UserAccounts user) {
        String username = normalizeNamePart(
                user.getUsername()
        );
        String lastname = normalizeNamePart(
                user.getLastname()
        );

        String displayName = java.util.stream.Stream
                .of(username, lastname)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "))
                .trim();

        return displayName.isBlank()
                ? username
                : displayName;
    }

    private String facultyDisplayName(PrimaryFaculty faculty) {
        String displayName = java.util.stream.Stream
                .of(
                        normalizeNamePart(faculty.getFirstname()),
                        normalizeNamePart(faculty.getMiddlename()),
                        normalizeNamePart(faculty.getLastname())
                )
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "))
                .trim();

        return displayName.isBlank()
                ? normalizeNamePart(faculty.getFacultyId())
                : displayName;
    }

    private String normalizeNamePart(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().replaceAll("\\s+", " ");
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
