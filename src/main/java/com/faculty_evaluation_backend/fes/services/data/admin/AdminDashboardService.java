package com.faculty_evaluation_backend.fes.services.data.admin;

import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardSummaryProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardSummaryResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageFacultyResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyEvaluationReadinessMetricsProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyEvaluationReadinessPageResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyEvaluationReadinessProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyEvaluationReadinessResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.SupervisorEvaluationDashboardMetricsProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.SupervisorEvaluationDashboardPageResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.SupervisorEvaluationDashboardProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.SupervisorEvaluationDashboardResponse;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.AdminDashboardRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.utilities.normalization.SemesterNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
class AdminDashboardService {

    private static final BigDecimal STANDARD_PREPARATION_LOAD_LIMIT =
            BigDecimal.valueOf(21);
    private static final BigDecimal HIGH_PREPARATION_LOAD_LIMIT =
            BigDecimal.valueOf(18);
    private static final int HIGH_PREPARATION_THRESHOLD = 3;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Set<String> ALLOWED_LEGACY_DATABASES = Set.of(
            "LEGACY_TALISAY",
            "LEGACY_ALIJIS",
            "LEGACY_FT",
            "LEGACY_BINALBAGAN"
    );
    private static final Set<String> SUPERVISOR_EVALUATION_STATUS_FILTERS =
            Set.of("EVALUATED", "PENDING");

    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    private final AdminDashboardRepository adminDashboardRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;

    AdminDashboardResponse getDashboard() {
        DashboardTerm term = resolveDashboardTerm();

        return AdminDashboardResponse.builder()
                .summary(buildDashboardSummary(term))
                .programs(findDashboardProgramBreakdown(term))
                .facultyLoads(findDashboardFacultyLoads(term, 10))
                .build();
    }

    AdminDashboardSummaryResponse getDashboardSummary() {
        return buildDashboardSummary(resolveDashboardTerm());
    }

    List<AdminDashboardProgramBreakdownResponse> getDashboardProgramBreakdown() {
        return findDashboardProgramBreakdown(resolveDashboardTerm());
    }

    List<AdminDashboardFacultyLoadResponse> getDashboardFacultyLoads(int limit) {
        return findDashboardFacultyLoads(resolveDashboardTerm(), limit);
    }

    SupervisorEvaluationDashboardPageResponse getSupervisorEvaluationDashboard(
            int page,
            int size,
            String search,
            String evaluationStatus,
            String legacyDatabase,
            String campus,
            Integer schoolYear,
            String semester
    ) {
        DashboardTerm activeTerm = resolveDashboardTerm();
        Integer resolvedSchoolYear =
                schoolYear == null ? activeTerm.schoolYear() : schoolYear;
        String resolvedSemester =
                normalizeBlank(semester) == null
                        ? activeTerm.semester()
                        : normalizeSemester(semester);
        String normalizedStatus =
                normalizeSupervisorEvaluationStatus(evaluationStatus);
        String normalizedSearch = normalizeBlank(search);
        String normalizedLegacyDatabase = normalizeLegacyDatabase(legacyDatabase);
        String normalizedCampus = normalizeBlank(campus);

        Pageable pageable = PageRequest.of(
                safePage(page),
                safePageSize(size)
        );

        Page<SupervisorEvaluationDashboardResponse> dashboardPage =
                facultyEvaluationScoreRepository
                        .findSupervisorEvaluationDashboard(
                                resolvedSchoolYear,
                                resolvedSemester,
                                normalizedSearch,
                                normalizedStatus,
                                normalizedLegacyDatabase,
                                normalizedCampus,
                                pageable
                        )
                        .map(projection ->
                                toSupervisorEvaluationDashboardResponse(
                                        projection,
                                        resolvedSchoolYear,
                                        resolvedSemester
                                )
                        );

        SupervisorEvaluationDashboardMetricsProjection metrics =
                facultyEvaluationScoreRepository
                        .findSupervisorEvaluationDashboardMetrics(
                                resolvedSchoolYear,
                                resolvedSemester,
                                normalizedSearch,
                                normalizedLegacyDatabase,
                                normalizedCampus
                        );
        long totalFacultyCount = metrics == null ? 0L : safeLong(metrics.getTotalFacultyCount());
        long evaluatedFacultyCount = metrics == null ? 0L : safeLong(metrics.getEvaluatedFacultyCount());
        long pendingFacultyCount = metrics == null ? 0L : safeLong(metrics.getPendingFacultyCount());

        return SupervisorEvaluationDashboardPageResponse.builder()
                .content(dashboardPage.getContent())
                .totalElements(dashboardPage.getTotalElements())
                .totalPages(dashboardPage.getTotalPages())
                .page(dashboardPage.getNumber())
                .size(dashboardPage.getSize())
                .totalFacultyCount(totalFacultyCount)
                .evaluatedFacultyCount(evaluatedFacultyCount)
                .pendingFacultyCount(pendingFacultyCount)
                .build();
    }


    FacultyEvaluationReadinessPageResponse getFacultyEvaluationReadiness(
            int page,
            int size,
            String search,
            College college,
            String legacyDatabase,
            String campus,
            Integer schoolYear,
            String semester
    ) {
        DashboardTerm activeTerm = resolveDashboardTerm();
        Integer resolvedSchoolYear =
                schoolYear == null ? activeTerm.schoolYear() : schoolYear;
        String resolvedSemester =
                normalizeBlank(semester) == null
                        ? activeTerm.semester()
                        : normalizeSemester(semester);
        String normalizedSearch = normalizeBlank(search);
        String normalizedLegacyDatabase = normalizeLegacyDatabase(legacyDatabase);
        String normalizedCampus = normalizeBlank(campus);
        String normalizedCollege = college == null ? null : college.name();

        Pageable pageable = PageRequest.of(safePage(page), safePageSize(size));

        Page<FacultyEvaluationReadinessResponse> readinessPage =
                facultyEvaluationScoreRepository
                        .findFacultyEvaluationReadiness(
                                resolvedSchoolYear,
                                resolvedSemester,
                                normalizedSearch,
                                normalizedCollege,
                                normalizedLegacyDatabase,
                                normalizedCampus,
                                pageable
                        )
                        .map(projection ->
                                toFacultyEvaluationReadinessResponse(
                                        projection,
                                        resolvedSchoolYear,
                                        resolvedSemester
                                )
                        );

        FacultyEvaluationReadinessMetricsProjection metrics =
                facultyEvaluationScoreRepository
                        .findFacultyEvaluationReadinessMetrics(
                                resolvedSchoolYear,
                                resolvedSemester,
                                normalizedSearch,
                                normalizedCollege,
                                normalizedLegacyDatabase,
                                normalizedCampus
                        );

        return FacultyEvaluationReadinessPageResponse.builder()
                .content(readinessPage.getContent())
                .totalElements(readinessPage.getTotalElements())
                .totalPages(readinessPage.getTotalPages())
                .page(readinessPage.getNumber())
                .size(readinessPage.getSize())
                .totalReadyFacultyCount(metrics == null ? 0L : safeLong(metrics.getTotalReadyFacultyCount()))
                .totalStudentEvaluationCount(metrics == null ? 0L : safeLong(metrics.getTotalStudentEvaluationCount()))
                .totalSupervisorEvaluationCount(metrics == null ? 0L : safeLong(metrics.getTotalSupervisorEvaluationCount()))
                .totalScoreRecordCount(metrics == null ? 0L : safeLong(metrics.getTotalScoreRecordCount()))
                .averageOverallScore(metrics == null ? BigDecimal.ZERO : safeBigDecimal(metrics.getAverageOverallScore()))
                .build();
    }

    List<String> findFacultyEvaluationReadinessFacultyIds(
            String search,
            College college,
            String legacyDatabase,
            String campus,
            Integer schoolYear,
            String semester
    ) {
        DashboardTerm activeTerm = resolveDashboardTerm();
        Integer resolvedSchoolYear =
                schoolYear == null ? activeTerm.schoolYear() : schoolYear;
        String resolvedSemester =
                normalizeBlank(semester) == null
                        ? activeTerm.semester()
                        : normalizeSemester(semester);

        return facultyEvaluationScoreRepository
                .findFacultyEvaluationReadinessFacultyIds(
                        resolvedSchoolYear,
                        resolvedSemester,
                        normalizeBlank(search),
                        college == null ? null : college.name(),
                        normalizeLegacyDatabase(legacyDatabase),
                        normalizeBlank(campus)
                );
    }
    FacultyWorkloadCoverageResponse getFacultyWorkloadCoverage() {
        DashboardTerm term = resolveDashboardTerm();
        List<FacultyWorkloadCoverageFacultyResponse> rows =
                primaryFacultyRepository
                        .findFacultyWorkloadCoverage(
                                term.schoolYear(),
                                term.semester(),
                                term.workloadSemester(),
                                Status.ACTIVE,
                                null
                        )
                        .stream()
                        .map(this::toFacultyWorkloadCoverageFacultyResponse)
                        .toList();

        List<FacultyWorkloadCoverageFacultyResponse> withWorkload =
                rows.stream()
                        .filter(FacultyWorkloadCoverageFacultyResponse::isHasWorkload)
                        .toList();
        List<FacultyWorkloadCoverageFacultyResponse> withoutWorkload =
                rows.stream()
                        .filter(row -> !row.isHasWorkload())
                        .toList();
        long totalActiveFaculty = rows.size();
        long withWorkloadCount = withWorkload.size();

        return FacultyWorkloadCoverageResponse.builder()
                .schoolYear(term.schoolYear())
                .semester(term.semester())
                .totalActiveFaculty(totalActiveFaculty)
                .withWorkloadCount(withWorkloadCount)
                .withoutWorkloadCount((long) withoutWorkload.size())
                .coverageRate(percentage(withWorkloadCount, totalActiveFaculty))
                .withWorkload(withWorkload)
                .withoutWorkload(withoutWorkload)
                .build();
    }

    private AdminDashboardSummaryResponse buildDashboardSummary(
            DashboardTerm term
    ) {
        AdminDashboardSummaryProjection summary =
                adminDashboardRepository.findSummary(
                        term.schoolYear(),
                        term.semester()
                );

        Long expectedEvaluations = safeLong(summary.getExpectedEvaluations());
        Long completedEvaluations = safeLong(summary.getCompletedEvaluations());

        return AdminDashboardSummaryResponse.builder()
                .schoolYear(term.schoolYear())
                .semester(term.semester())
                .totalStudents(safeLong(summary.getTotalStudents()))
                .totalFaculty(safeLong(summary.getTotalFaculty()))
                .totalClasses(safeLong(summary.getTotalClasses()))
                .totalSubjects(safeLong(summary.getTotalSubjects()))
                .totalPrograms(safeLong(summary.getTotalPrograms()))
                .totalSections(safeLong(summary.getTotalSections()))
                .expectedEvaluations(expectedEvaluations)
                .completedEvaluations(completedEvaluations)
                .evaluatedStudents(safeLong(summary.getEvaluatedStudents()))
                .pendingEvaluations(Math.max(
                        expectedEvaluations - completedEvaluations,
                        0
                ))
                .evaluationCompletionRate(
                        percentage(completedEvaluations, expectedEvaluations)
                )
                .averageOverallScore(safeDouble(summary.getAverageOverallScore()))
                .build();
    }

    private List<AdminDashboardProgramBreakdownResponse>
    findDashboardProgramBreakdown(DashboardTerm term) {
        return adminDashboardRepository
                .findProgramBreakdown(term.schoolYear(), term.semester())
                .stream()
                .map(this::toProgramBreakdownResponse)
                .toList();
    }

    private List<AdminDashboardFacultyLoadResponse> findDashboardFacultyLoads(
            DashboardTerm term,
            int limit
    ) {
        int safeLimit = Math.min(Math.max(limit, 1), 50);

        return adminDashboardRepository
                .findTopFacultyLoads(
                        term.schoolYear(),
                        term.semester(),
                        safeLimit
                )
                .stream()
                .map(this::toFacultyLoadResponse)
                .toList();
    }

    private AdminDashboardProgramBreakdownResponse
    toProgramBreakdownResponse(
            AdminDashboardProgramBreakdownProjection projection
    ) {
        Long expected = safeLong(projection.getExpectedEvaluations());
        Long completed = safeLong(projection.getCompletedEvaluations());

        return AdminDashboardProgramBreakdownResponse.builder()
                .programCode(projection.getProgramCode())
                .totalStudents(safeLong(projection.getTotalStudents()))
                .totalClasses(safeLong(projection.getTotalClasses()))
                .totalSections(safeLong(projection.getTotalSections()))
                .expectedEvaluations(expected)
                .completedEvaluations(completed)
                .completionRate(percentage(completed, expected))
                .averageOverallScore(safeDouble(
                        projection.getAverageOverallScore()
                ))
                .build();
    }

    private AdminDashboardFacultyLoadResponse toFacultyLoadResponse(
            AdminDashboardFacultyLoadProjection projection
    ) {
        return AdminDashboardFacultyLoadResponse.builder()
                .facultyId(projection.getFacultyId())
                .facultyName(projection.getFacultyName())
                .totalClasses(safeLong(projection.getTotalClasses()))
                .totalSubjects(safeLong(projection.getTotalSubjects()))
                .totalStudents(safeLong(projection.getTotalStudents()))
                .completedEvaluations(safeLong(
                        projection.getCompletedEvaluations()
                ))
                .averageOverallScore(safeDouble(
                        projection.getAverageOverallScore()
                ))
                .build();
    }

    private FacultyWorkloadCoverageFacultyResponse
    toFacultyWorkloadCoverageFacultyResponse(
            FacultyWorkloadCoverageProjection projection
    ) {
        Long workloadCount = safeLong(projection.getWorkloadCount());
        Integer numberOfPreparations = projection.getNumberOfPreparations();
        College college = projection.getCollege();
        Status status = projection.getStatus();

        return FacultyWorkloadCoverageFacultyResponse.builder()
                .facultyId(projection.getFacultyId())
                .facultyName(fullFacultyName(
                        projection.getFirstname(),
                        projection.getMiddlename(),
                        projection.getLastname(),
                        projection.getFacultyId()
                ))
                .position(projection.getPosition())
                .college(college)
                .status(status)
                .loadLimit(numberOfPreparations == null
                        ? projection.getLoadLimit()
                        : effectiveLoadLimit(numberOfPreparations).doubleValue())
                .hasWorkload(workloadCount > 0)
                .workloadCount(workloadCount)
                .totalHoursPerWeek(safeBigDecimal(
                        projection.getTotalHoursPerWeek()
                ))
                .numberOfPreparations(numberOfPreparations)
                .build();
    }


    private FacultyEvaluationReadinessResponse
    toFacultyEvaluationReadinessResponse(
            FacultyEvaluationReadinessProjection projection,
            Integer schoolYear,
            String semester
    ) {
        return FacultyEvaluationReadinessResponse.builder()
                .facultyId(projection.getFacultyId())
                .facultyName(projection.getFacultyName())
                .position(projection.getPosition())
                .college(projection.getCollege())
                .legacyDatabase(projection.getLegacyDatabase())
                .campus(projection.getCampus())
                .schoolYear(schoolYear)
                .semester(semester)
                .subjects(splitSubjects(projection.getSubjects()))
                .studentEvaluationCount(safeLong(projection.getStudentEvaluationCount()))
                .supervisorEvaluationCount(safeLong(projection.getSupervisorEvaluationCount()))
                .totalScoreRecords(safeLong(projection.getTotalScoreRecords()))
                .setAverage(safeBigDecimal(projection.getSetAverage()))
                .sefAverage(safeBigDecimal(projection.getSefAverage()))
                .overallAverage(safeBigDecimal(projection.getOverallAverage()))
                .lastEvaluatedAt(projection.getLastEvaluatedAt())
                .build();
    }

    private List<String> splitSubjects(String subjects) {
        if (subjects == null || subjects.isBlank()) {
            return List.of();
        }

        return Arrays.stream(subjects.split(","))
                .map(String::trim)
                .filter(subject -> !subject.isBlank())
                .toList();
    }
    private SupervisorEvaluationDashboardResponse
    toSupervisorEvaluationDashboardResponse(
            SupervisorEvaluationDashboardProjection projection,
            Integer schoolYear,
            String semester
    ) {
        Long supervisorEvaluationCount =
                safeLong(projection.getSupervisorEvaluationCount());

        return SupervisorEvaluationDashboardResponse.builder()
                .facultyId(projection.getFacultyId())
                .facultyName(projection.getFacultyName())
                .position(projection.getPosition())
                .college(projection.getCollege())
                .legacyDatabase(projection.getLegacyDatabase())
                .campus(projection.getCampus())
                .assignedClassCount(safeLong(projection.getAssignedClassCount()))
                .supervisorEvaluated(supervisorEvaluationCount > 0)
                .supervisorEvaluationCount(supervisorEvaluationCount)
                .supervisorIds(projection.getSupervisorIds())
                .supervisorNames(projection.getSupervisorNames())
                .supervisorPositions(projection.getSupervisorPositions())
                .supervisorAverageScore(projection.getSupervisorAverageScore())
                .lastEvaluatedAt(projection.getLastEvaluatedAt())
                .schoolYear(schoolYear)
                .semester(semester)
                .build();
    }

    private DashboardTerm resolveDashboardTerm() {
        SchoolYearAndSemester activeTerm =
                schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No active semester found."
                                )
                        );

        return new DashboardTerm(
                activeTerm.getSchoolYear(),
                activeTerm.getSemester().getValue(),
                activeTerm.getSemester().name()
        );
    }

    private String fullFacultyName(
            String firstname,
            String middlename,
            String lastname,
            String fallback
    ) {
        String name = String.join(
                        " ",
                        firstname == null ? "" : firstname.trim(),
                        middlename == null ? "" : middlename.trim(),
                        lastname == null ? "" : lastname.trim()
                )
                .replaceAll("\\s+", " ")
                .trim();

        return name.isBlank() ? fallback : name;
    }

    private BigDecimal effectiveLoadLimit(Integer numberOfPreparations) {
        if (numberOfPreparations != null
                && numberOfPreparations >= HIGH_PREPARATION_THRESHOLD) {
            return HIGH_PREPARATION_LOAD_LIMIT;
        }

        return STANDARD_PREPARATION_LOAD_LIMIT;
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private Double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private Double percentage(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0L) {
            return 0.0;
        }

        return Math.round((numerator * 10000.0) / denominator) / 100.0;
    }

    private String normalizeBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safePageSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalizeSemester(String value) {
        return SemesterNormalizer.toCanonicalValue(value);
    }

    private String normalizeLegacyDatabase(String legacyDatabase) {
        if (legacyDatabase == null || legacyDatabase.trim().isEmpty()) {
            return null;
        }

        String normalized = legacyDatabase.trim().toUpperCase();

        if (!ALLOWED_LEGACY_DATABASES.contains(normalized)) {
            throw new BadRequestException("Invalid legacy database filter: " + legacyDatabase);
        }

        return normalized;
    }

    private String normalizeSupervisorEvaluationStatus(String status) {
        String normalized = normalizeBlank(status);
        if (normalized == null || normalized.equalsIgnoreCase("ALL")) {
            return null;
        }

        normalized = normalized.toUpperCase();
        if (!SUPERVISOR_EVALUATION_STATUS_FILTERS.contains(normalized)) {
            throw new BadRequestException(
                    "Invalid supervisor evaluation status filter: " + status
            );
        }

        return normalized;
    }

    private record DashboardTerm(
            Integer schoolYear,
            String semester,
            String workloadSemester
    ) {
    }
}
