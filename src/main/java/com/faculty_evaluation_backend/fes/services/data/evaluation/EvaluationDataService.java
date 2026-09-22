package com.faculty_evaluation_backend.fes.services.data.evaluation;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.EvaluationSubmissionResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationBulkReportResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationPrintEventResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationPrintResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationGeneratedReportResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationReportPrintTrackingProjection;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationReportPrintTrackingResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationReportVerificationResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
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
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationPrintEventType;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.FacultyEvaluationReportStatus;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.RatingScale;
import com.faculty_evaluation_backend.fes.entities.primary.FacultyWorkload;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.EvaluationType;
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
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.Comparator;
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
    private static final int DEFAULT_PRINT_TRACKING_PAGE_SIZE = 10;
    private static final int MAX_PRINT_TRACKING_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_LEGACY_DATABASES = Set.of(
            "LEGACY_TALISAY",
            "LEGACY_ALIJIS",
            "LEGACY_FT",
            "LEGACY_BINALBAGAN"
    );
    private static final Set<String> ALLOWED_REPORT_STATUSES = Set.of(
            "VALID",
            "SUPERSEDED",
            "REVOKED"
    );
    private static final Set<String> ALLOWED_PRINT_STATUS_FILTERS = Set.of(
            "ALL",
            "REPORT_PRINTED",
            "REPORT_NOT_PRINTED",
            "ANNEX_D_PRINTED",
            "ANNEX_D_NOT_PRINTED"
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
            @CacheEvict(value = "facultyEvaluationReports", key = "#baseEvaluationDTO.facultyId"),
            @CacheEvict(
                    value = "studentFacultyClassEvaluationChecks",
                    key = "(#baseEvaluationDTO.facultyId == null ? '' : #baseEvaluationDTO.facultyId.trim()) + ':' + "
                            + "(#baseEvaluationDTO.evaluatorId == null ? '' : #baseEvaluationDTO.evaluatorId.trim()) + ':' + "
                            + "(#baseEvaluationDTO.classCode == null ? '' : #baseEvaluationDTO.classCode.trim()) + ':' + "
                            + "(#baseEvaluationDTO.subjectCode == null ? '' : #baseEvaluationDTO.subjectCode.trim()) + ':' + "
                            + "(#baseEvaluationDTO.yearLevel == null ? '' : #baseEvaluationDTO.yearLevel.trim()) + ':' + "
                            + "(#baseEvaluationDTO.semester == null ? '' : #baseEvaluationDTO.semester.trim()) + ':' + "
                            + "#baseEvaluationDTO.schoolYear"
            )
    })
    public EvaluationSubmissionResponse submit(BaseEvaluationDTO baseEvaluationDTO) {
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
        FacultyEvaluationScore savedEvaluationScore =
                facultyEvaluationScoreRepository.save(evaluationScore);

        refreshFacultyEvaluationReadinessSummary(savedEvaluationScore);

        return EvaluationSubmissionResponse.from(savedEvaluationScore);
    }


    private void refreshFacultyEvaluationReadinessSummary(
            FacultyEvaluationScore evaluationScore
    ) {
        facultyEvaluationScoreRepository
                .deleteFacultyEvaluationReadinessSummary(
                        evaluationScore.getFacultyId(),
                        evaluationScore.getSchoolYear(),
                        evaluationScore.getSemester()
                );
        facultyEvaluationScoreRepository
                .insertFacultyEvaluationReadinessSummary(
                        evaluationScore.getFacultyId(),
                        evaluationScore.getSchoolYear(),
                        evaluationScore.getSemester()
                );
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
        SchoolYearAndSemester schoolYearAndSemester = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active school year and semester found."));
        return getSumOfAllFacultyEvaluationPerSubject(facultyId, schoolYearAndSemester);
    }

    public List<FacultyEvaluationPrintResponse> getSumOfAllFacultyEvaluationPerSubject(
            String facultyId,
            SchoolYearAndSemester schoolYearAndSemester
    ) {
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
                if (isStudentEvaluation(evaluation, studentEvaluatorIds)) {

                    studentEvaluations.add(evaluation);

                } else if (isSupervisorEvaluation(evaluation, studentEvaluatorIds)) {

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
            if (isStudentEvaluation(first, studentEvaluatorIds)) {

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

                    .facultyName(facultyDisplayName(first.getFaculty()))

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

                    .numberOfSupervisors(supervisorEvaluations.size())

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
        Long generatedByUserId = resolveGeneratedByUserId(generatedBy);
        String generatedByUsername = preparedByName(generatedByUserId, generatedBy);
        return generateFacultyEvaluationReportInternal(
                facultyId,
                verificationBaseUrl,
                term,
                generatedByUserId,
                generatedByUsername
        );
    }

    private FacultyEvaluationGeneratedReportResponse generateFacultyEvaluationReportInternal(
            String facultyId,
            String verificationBaseUrl,
            SchoolYearAndSemester term,
            Long generatedByUserId,
            String generatedByUsername
    ) {
        List<FacultyEvaluationPrintResponse> items =
                getSumOfAllFacultyEvaluationPerSubject(facultyId, term);

        if (items.isEmpty()) {
            throw new BadRequestException(
                    "No printable evaluated workload subjects found for this faculty."
            );
        }

        List<FacultyEvaluationReport> currentValidReports =
                facultyEvaluationReportRepository
                .findByFacultyIdAndSchoolYearAndSemesterAndStatus(
                        facultyId,
                        term.getSchoolYear(),
                        term.getSemester().getValue(),
                        FacultyEvaluationReportStatus.VALID
                );
        String facultyName = items.getFirst().getFacultyName();
        Optional<FacultyEvaluationReport> reusableReport =
                currentValidReports.stream()
                        .filter(report ->
                                reportContentMatches(
                                        report,
                                        facultyId,
                                        facultyName,
                                        term.getSchoolYear(),
                                        term.getSemester().getValue(),
                                        items
                                )
                        )
                        .max(Comparator.comparing(FacultyEvaluationReport::getGeneratedAt));

        if (reusableReport.isPresent()) {
            FacultyEvaluationReport report = reusableReport.get();

            return toGeneratedReportResponse(
                    report,
                    items,
                    qrCodeDataUri(report.getVerificationUrl())
            );
        }

        currentValidReports.forEach(report ->
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

        return generateBulkReportsForFacultyIds(facultyIds, verificationBaseUrl, generatedBy);
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

        return generateBulkReportsForFacultyIds(facultyIds, verificationBaseUrl, generatedBy);
    }

    private FacultyEvaluationBulkReportResponse generateBulkReportsForFacultyIds(
            List<String> facultyIds,
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
        Long generatedByUserId = resolveGeneratedByUserId(generatedBy);
        String generatedByUsername = preparedByName(generatedByUserId, generatedBy);

        List<FacultyEvaluationGeneratedReportResponse> reports =
                new ArrayList<>();
        List<String> skippedFacultyIds = new ArrayList<>();

        for (String facultyId : facultyIds) {
            try {
                reports.add(
                        generateFacultyEvaluationReportInternal(
                                facultyId,
                                verificationBaseUrl,
                                term,
                                generatedByUserId,
                                generatedByUsername
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
    public PageResponse<FacultyEvaluationReportPrintTrackingResponse>
    getFacultyEvaluationReportPrintTracking(
            int page,
            int size,
            String search,
            String status,
            String printStatus,
            Integer schoolYear,
            String semester
    ) {
        SchoolYearAndSemester activeTerm =
                schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "No active school year and semester found."
                                )
                        );
        Integer resolvedSchoolYear =
                schoolYear == null
                        ? activeTerm.getSchoolYear()
                        : schoolYear;
        String resolvedSemester =
                normalizeBlank(semester) == null
                        ? activeTerm.getSemester().getValue()
                        : SemesterNormalizer.toCanonicalValue(semester);
        String normalizedStatus = normalizeReportStatus(status);
        String normalizedPrintStatus = normalizePrintStatus(printStatus);
        Pageable pageable =
                PageRequest.of(
                        Math.max(page, 0),
                        safePrintTrackingPageSize(size)
                );

        Page<FacultyEvaluationReportPrintTrackingResponse> trackingPage =
                facultyEvaluationReportRepository
                        .findPrintTrackingReports(
                                resolvedSchoolYear,
                                resolvedSemester,
                                normalizeBlank(search),
                                normalizedStatus,
                                normalizedPrintStatus,
                                pageable
                        )
                        .map(this::toPrintTrackingResponse);

        return PageResponse
                .<FacultyEvaluationReportPrintTrackingResponse>builder()
                .content(trackingPage.getContent())
                .totalElements(trackingPage.getTotalElements())
                .totalPages(trackingPage.getTotalPages())
                .page(trackingPage.getNumber())
                .size(trackingPage.getSize())
                .build();
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyEvaluationPrintEventResponse markFacultyEvaluationReportsPrinted(
            List<String> reportIds,
            FacultyEvaluationPrintEventType type,
            CustomUserDetails printedBy
    ) {
        if (reportIds == null || reportIds.isEmpty()) {
            throw new BadRequestException("No report IDs were provided.");
        }

        if (type == null) {
            throw new BadRequestException("Print event type is required.");
        }

        List<String> normalizedReportIds =
                reportIds.stream()
                        .map(this::normalizeBlank)
                        .filter(value -> value != null && !value.isBlank())
                        .distinct()
                        .toList();

        if (normalizedReportIds.isEmpty()) {
            throw new BadRequestException("No valid report IDs were provided.");
        }

        List<FacultyEvaluationReport> reports =
                facultyEvaluationReportRepository.findAllById(normalizedReportIds);
        Set<String> foundReportIds =
                reports.stream()
                        .map(FacultyEvaluationReport::getReportId)
                        .collect(Collectors.toSet());
        List<String> missingReportIds =
                normalizedReportIds.stream()
                        .filter(reportId -> !foundReportIds.contains(reportId))
                        .toList();

        Instant printedAt = Instant.now();
        Long printedByUserId = resolveGeneratedByUserId(printedBy);
        String printedByUsername = preparedByName(printedByUserId, printedBy);

        reports.forEach(report ->
                applyPrintEvent(
                        report,
                        type,
                        printedAt,
                        printedByUserId,
                        printedByUsername
                )
        );

        facultyEvaluationReportRepository.saveAll(reports);

        return FacultyEvaluationPrintEventResponse.builder()
                .type(type)
                .requestedCount(normalizedReportIds.size())
                .updatedCount(reports.size())
                .missingReportIds(missingReportIds)
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

    private boolean isStudentEvaluation(
            FacultyEvaluationScore evaluation,
            Set<String> studentEvaluatorIds
    ) {
        EvaluationType evaluationType = evaluation.getEvaluationType();

        if (evaluationType != null) {
            return evaluationType == EvaluationType.ROLE_STUDENT;
        }

        return studentEvaluatorIds.contains(evaluation.getEvaluatorId());
    }

    private boolean isSupervisorEvaluation(
            FacultyEvaluationScore evaluation,
            Set<String> studentEvaluatorIds
    ) {
        EvaluationType evaluationType = evaluation.getEvaluationType();

        if (evaluationType != null) {
            return evaluationType == EvaluationType.ROLE_DEAN
                    || evaluationType == EvaluationType.ROLE_PROGRAM_CHAIR;
        }

        return !studentEvaluatorIds.contains(evaluation.getEvaluatorId());
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
            List<FacultyEvaluationPrintResponse> supervisorOnlyResponses =
                    responses.stream()
                            .filter(this::hasSupervisorEvaluation)
                            .map(this::supervisorOnlyPrintResponse)
                            .toList();

            if (!supervisorOnlyResponses.isEmpty()) {
                return supervisorOnlyResponses;
            }

            throw new BadRequestException(
                    "Faculty only has overload subjects, not allowed for SET printing"
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

        List<FacultyEvaluationPrintResponse> printableResponses =
                new ArrayList<>();

        for (FacultyEvaluationPrintResponse response : responses) {
            boolean regularWorkloadMatch =
                    findMatchingWorkload(
                            response,
                            workloadsByClassCode,
                            workloadsByCourseSection
                    ).isPresent();

            if (regularWorkloadMatch) {
                printableResponses.add(response);
            } else if (hasSupervisorEvaluation(response)) {
                printableResponses.add(supervisorOnlyPrintResponse(response));
            }
        }

        return printableResponses;
    }

    private boolean hasSupervisorEvaluation(
            FacultyEvaluationPrintResponse response
    ) {
        return response.getNumberOfSupervisors() != null
                && response.getNumberOfSupervisors() > 0;
    }

    private FacultyEvaluationPrintResponse supervisorOnlyPrintResponse(
            FacultyEvaluationPrintResponse response
    ) {
        return FacultyEvaluationPrintResponse.builder()
                .facultyEvaluationScoreId(response.getFacultyEvaluationScoreId())
                .facultyId(response.getFacultyId())
                .facultyName(response.getFacultyName())
                .evaluatorId(response.getEvaluatorId())
                .evaluatorType("SEF")
                .classCode(response.getClassCode())
                .college(response.getCollege())
                .sectionCode(response.getSectionCode())
                .programCode(response.getProgramCode())
                .position(response.getPosition())
                .semester(response.getSemester())
                .schoolYear(response.getSchoolYear())
                .subjectCode(response.getSubjectCode())
                .yearLevel(response.getYearLevel())
                .overallAverageScore(0.0)
                .overallInterpretation(determineInterpretation(0.0))
                .numberOfStudents(0)
                .numberOfSupervisors(response.getNumberOfSupervisors())
                .setRating(0.0)
                .sefRating(response.getSefRating())
                .studentComments("-")
                .supervisorComments(response.getSupervisorComments())
                .supervisorName(response.getSupervisorName())
                .supervisorDesignation(response.getSupervisorDesignation())
                .build();
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

    private void applyPrintEvent(
            FacultyEvaluationReport report,
            FacultyEvaluationPrintEventType type,
            Instant printedAt,
            Long printedByUserId,
            String printedByUsername
    ) {
        report.setPrintTrackingAvailable(true);

        if (type == FacultyEvaluationPrintEventType.ANNEX_D) {
            if (report.getAnnexDPrintedAt() == null) {
                report.setAnnexDPrintedAt(printedAt);
            }

            report.setAnnexDPrintedByUserId(printedByUserId);
            report.setAnnexDPrintedByUsername(printedByUsername);
            report.setAnnexDPrintCount(safeInteger(report.getAnnexDPrintCount()) + 1);
            return;
        }

        if (report.getPrintedAt() == null) {
            report.setPrintedAt(printedAt);
        }

        report.setPrintedByUserId(printedByUserId);
        report.setPrintedByUsername(printedByUsername);
        report.setPrintCount(safeInteger(report.getPrintCount()) + 1);
    }

    private FacultyEvaluationReportPrintTrackingResponse toPrintTrackingResponse(
            FacultyEvaluationReportPrintTrackingProjection projection
    ) {
        return FacultyEvaluationReportPrintTrackingResponse.builder()
                .reportId(projection.getReportId())
                .facultyId(projection.getFacultyId())
                .facultyName(projection.getFacultyName())
                .schoolYear(projection.getSchoolYear())
                .semester(projection.getSemester())
                .versionNumber(projection.getVersionNumber())
                .status(projection.getStatus())
                .generatedByUsername(projection.getGeneratedByUsername())
                .generatedAt(projection.getGeneratedAt())
                .printTrackingAvailable(projection.getPrintTrackingAvailable())
                .printedAt(projection.getPrintedAt())
                .printedByUsername(projection.getPrintedByUsername())
                .printCount(safeInteger(projection.getPrintCount()))
                .annexDPrintedAt(projection.getAnnexDPrintedAt())
                .annexDPrintedByUsername(projection.getAnnexDPrintedByUsername())
                .annexDPrintCount(safeInteger(projection.getAnnexDPrintCount()))
                .build();
    }

    private int safePrintTrackingPageSize(int size) {
        if (size <= 0) {
            return DEFAULT_PRINT_TRACKING_PAGE_SIZE;
        }

        return Math.min(size, MAX_PRINT_TRACKING_PAGE_SIZE);
    }

    private Integer safeInteger(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeReportStatus(String status) {
        String normalized = normalizeBlank(status);
        if (normalized == null || normalized.equalsIgnoreCase("ALL")) {
            return null;
        }

        normalized = normalized.toUpperCase();
        if (!ALLOWED_REPORT_STATUSES.contains(normalized)) {
            throw new BadRequestException("Invalid report status filter: " + status);
        }

        return normalized;
    }

    private String normalizePrintStatus(String printStatus) {
        String normalized = normalizeBlank(printStatus);
        if (normalized == null || normalized.equalsIgnoreCase("ALL")) {
            return null;
        }

        normalized = normalized.toUpperCase();
        if (!ALLOWED_PRINT_STATUS_FILTERS.contains(normalized)) {
            throw new BadRequestException("Invalid print status filter: " + printStatus);
        }

        return normalized;
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
                .printTrackingAvailable(report.getPrintTrackingAvailable())
                .printedAt(report.getPrintedAt())
                .printedByUsername(report.getPrintedByUsername())
                .printCount(report.getPrintCount())
                .annexDPrintedAt(report.getAnnexDPrintedAt())
                .annexDPrintedByUsername(report.getAnnexDPrintedByUsername())
                .annexDPrintCount(report.getAnnexDPrintCount())
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

    private boolean reportContentMatches(
            FacultyEvaluationReport report,
            String facultyId,
            String facultyName,
            Integer schoolYear,
            String semester,
            List<FacultyEvaluationPrintResponse> items
    ) {
        if (report.getSnapshotJson() == null
                || report.getSnapshotJson().isBlank()) {
            return false;
        }

        try {
            JsonNode snapshot = objectMapper.readTree(report.getSnapshotJson());

            return textNodeEquals(snapshot, "facultyId", facultyId)
                    && textNodeEquals(snapshot, "facultyName", facultyName)
                    && intNodeEquals(snapshot, "schoolYear", schoolYear)
                    && textNodeEquals(
                            snapshot,
                            "semester",
                            SemesterNormalizer.toCanonicalValue(semester)
                    )
                    && snapshot.path("items").equals(objectMapper.valueToTree(items));
        } catch (JsonProcessingException exception) {
            log.warn(
                    "Unable to compare faculty evaluation report snapshot | reportId={}",
                    report.getReportId(),
                    exception
            );
            return false;
        }
    }

    private boolean textNodeEquals(
            JsonNode node,
            String fieldName,
            String expected
    ) {
        String actual = node.path(fieldName).asText("");
        String normalizedExpected = expected == null ? "" : expected;

        return actual.equals(normalizedExpected);
    }

    private boolean intNodeEquals(
            JsonNode node,
            String fieldName,
            Integer expected
    ) {
        return expected != null
                && node.path(fieldName).canConvertToInt()
                && node.path(fieldName).asInt() == expected;
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

    private String normalizeBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
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
        if (generatedByUserId != null) {
            String displayName = userAccountsRepository
                    .findById(generatedByUserId)
                    .map(this::preparedByName)
                    .orElse(null);

            if (displayName != null && !displayName.isBlank()) {
                return displayName;
            }
        }

        if (generatedBy != null) {
            return preparedByName(generatedBy.getUser());
        }

        return null;
    }

    private String preparedByName(UserAccounts user) {
        String firstname = normalizeNamePart(
                user.getFirstname()
        );
        String lastname = normalizeNamePart(
                user.getLastname()
        );
        String username = normalizeNamePart(
                user.getUsername()
        );

        String displayName = java.util.stream.Stream
                .of(firstname, lastname)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "))
                .trim();

        return displayName.isBlank()
                ? username
                : displayName;
    }

    private String facultyDisplayName(PrimaryFaculty faculty) {
        if (faculty == null) {
            return null;
        }

        String lastname = normalizeNamePart(faculty.getLastname());
        String firstname = normalizeNamePart(faculty.getFirstname());
        String middlename = middleNameOrInitial(faculty.getMiddlename());
        String givenName = java.util.stream.Stream
                .of(firstname, middlename)
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.joining(" "));
        String displayName = lastname == null
                ? givenName
                : givenName.isBlank()
                        ? lastname
                        : lastname + ", " + givenName;

        return displayName.isBlank()
                ? normalizeNamePart(faculty.getFacultyId())
                : displayName;
    }

    private String middleNameOrInitial(String value) {
        String normalized = normalizeNamePart(value);
        if (normalized == null) {
            return null;
        }

        return normalized.length() == 1
                ? normalized + "."
                : normalized;
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

