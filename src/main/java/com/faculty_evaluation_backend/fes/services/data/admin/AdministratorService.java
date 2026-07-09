package com.faculty_evaluation_backend.fes.services.data.admin;

import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardSummaryResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardSummaryProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageFacultyResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentEvaluationStatusResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentFacultyEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyWorkloadClassOptionResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.ClassFacultyAssignmentResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.ClassFacultyReassignmentRequest;
import com.faculty_evaluation_backend.fes.dto.faculty.ClassFacultyReassignmentResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyAssignmentOptionResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyWorkloadRequest;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyWorkloadResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyWorkloadSectionOptionResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyEvaluationScoreResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchUserAccountsResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.dto.student.StudentSectionDTO;
import com.faculty_evaluation_backend.fes.dto.user_accounts.CreateUserAccountDTO;
import com.faculty_evaluation_backend.fes.dto.user_accounts.UpdateUserAccountDTO;
import com.faculty_evaluation_backend.fes.dto.user_accounts.UpdateUserPasswordDTO;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.primary.FacultyWorkload;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryClass;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.FacultyWorkloadSource;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.AdminDashboardRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.FacultyWorkloadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySectionRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.utilities.mapper.PageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdministratorService {

    private static final BigDecimal STANDARD_PREPARATION_LOAD_LIMIT =
            BigDecimal.valueOf(21);
    private static final BigDecimal HIGH_PREPARATION_LOAD_LIMIT =
            BigDecimal.valueOf(18);
    private static final int HIGH_PREPARATION_THRESHOLD = 3;

    private static final Set<String> ALLOWED_LEGACY_DATABASES = Set.of(
            "LEGACY_TALISAY",
            "LEGACY_ALIJIS",
            "LEGACY_FT",
            "LEGACY_BINALBAGAN"
    );

    private final PrimaryFacultyRepository primaryFacultyRepository;

    private final UserAccountsRepository userAccountsRepository;

    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;

    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;

    private final PasswordEncoder passwordEncoder;
    private final PrimarySectionRepository primarySectionRepository;
    private final PrimaryClassRepository primaryClassRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final AdminDashboardRepository adminDashboardRepository;
    private final FacultyWorkloadRepository facultyWorkloadRepository;

    @Transactional
    @CacheEvict(
            value = {
                    "currentSchoolYearSemester",
                    "adminDashboard",
                    "adminDashboardSummary",
                    "adminDashboardPrograms",
                    "adminDashboardFacultyLoads",
                    "facultyWorkloadCoverage",
                    "facultyWorkloadClassOptions",
                    "facultyClasses",
                    "facultyEvaluationReports",
                    "studentSections",
                    "studentEvaluationStatus",
                    "studentFacultyEvaluations",
                    "facultyEvaluationScores",
                    "studentLoads",
                    "studentFacultyClassEvaluationChecks",
                    "supervisorFacultyLoads",
                    "supervisorFacultyProgramLoads",
                    "supervisorEvaluatedStudents"
            },
            allEntries = true
    )
    public SchoolYearAndSemesterDTO updateSchoolYearAndSemester(Integer schoolYear, Semester semester) {

        if (schoolYear == null) {

            throw new BadRequestException("School year is required.");
        }

        if (semester == null) {

            throw new BadRequestException("Semester is required.");
        }

        schoolYearAndSemesterRepository.findBySchoolYearAndSemesterAndStatus(schoolYear, semester, Status.ACTIVE).ifPresent(existing -> {

            throw new BadRequestException("The selected school year and semester is already active.");
        });

        schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).ifPresent(existing -> {

            existing.setStatus(Status.INACTIVE);

            schoolYearAndSemesterRepository.save(existing);
        });

        SchoolYearAndSemester newRecord = SchoolYearAndSemester.builder().schoolYear(schoolYear).semester(semester).status(Status.ACTIVE).build();

        SchoolYearAndSemester savedRecord = schoolYearAndSemesterRepository.save(newRecord);

        return SchoolYearAndSemesterDTO.builder().id(savedRecord.getId()).schoolYear(savedRecord.getSchoolYear()).semester(savedRecord.getSemester()).status(savedRecord.getStatus()).createdAt(savedRecord.getCreatedAt()).build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "currentSchoolYearSemester", key = "'active'")
    public SchoolYearAndSemesterDTO fetchCurrentSchoolYearAndSemester() {

        SchoolYearAndSemester data = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).orElseThrow(() -> new ResourceNotFoundException("No active semester found."));

        return SchoolYearAndSemesterDTO.builder().id(data.getId()).schoolYear(data.getSchoolYear()).semester(data.getSemester()).status(data.getStatus()).createdAt(data.getCreatedAt()).build();
    }

    @Transactional(readOnly = true)
    public PageResponse<ClassFacultyAssignmentResponse> getClassAssignments(
            int page,
            int size,
            String search,
            String legacyDatabase
    ) {
        DashboardTerm term = resolveDashboardTerm();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(
                        Sort.Order.asc("subjectCode"),
                        Sort.Order.asc("classCode"),
                        Sort.Order.asc("primaryClassId")
                )
        );

        Page<ClassFacultyAssignmentResponse> assignments =
                primaryClassRepository.findAdminClassAssignments(
                        term.schoolYear(),
                        term.semester(),
                        normalizeOptional(search),
                        normalizeOptional(legacyDatabase),
                        pageable
                ).map(this::toClassFacultyAssignmentResponse);

        return PageMapper.toPageResponse(assignments);
    }

    @Transactional(readOnly = true)
    public List<FacultyAssignmentOptionResponse>
    getClassAssignmentFacultyOptions(String legacyDatabase) {
        return primaryFacultyRepository.findAssignmentOptions(
                        Status.ACTIVE,
                        normalizeOptional(legacyDatabase)
                ).stream()
                .map(this::toFacultyAssignmentOptionResponse)
                .toList();
    }

    @Transactional
    @CacheEvict(
            value = {
                    "adminDashboard",
                    "adminDashboardSummary",
                    "adminDashboardPrograms",
                    "adminDashboardFacultyLoads",
                    "facultyWorkloadCoverage",
                    "facultyWorkloadClassOptions",
                    "facultyClasses",
                    "facultyEvaluationReports",
                    "studentSections",
                    "studentEvaluationStatus",
                    "studentFacultyEvaluations",
                    "facultyEvaluationScores",
                    "studentLoads",
                    "studentFacultyClassEvaluationChecks",
                    "supervisorFacultyLoads",
                    "supervisorFacultyProgramLoads",
                    "supervisorEvaluatedStudents"
            },
            allEntries = true
    )
    public ClassFacultyReassignmentResponse reassignClassFaculty(
            Long primaryClassId,
            ClassFacultyReassignmentRequest request
    ) {
        if (primaryClassId == null) {
            throw new BadRequestException("Primary class ID is required.");
        }

        PrimaryClass primaryClass = primaryClassRepository
                .findAssignmentById(primaryClassId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Class assignment not found.")
                );

        DashboardTerm term = resolveDashboardTerm();
        if (!Objects.equals(primaryClass.getSchoolYear(), term.schoolYear())
                || !term.semester().equalsIgnoreCase(
                        primaryClass.getSemester()
                )) {
            throw new BadRequestException(
                    "Only classes in the current school year and semester can be reassigned."
            );
        }

        String previousFacultyId = normalizeOptional(
                primaryClass.getFacultyId()
        );
        String expectedFacultyId = normalizeOptional(
                request.expectedCurrentFacultyId()
        );
        if (!Objects.equals(previousFacultyId, expectedFacultyId)) {
            throw new BadRequestException(
                    "This class assignment has changed. Refresh the list and try again."
            );
        }

        String newFacultyId = request.facultyId().trim();
        PrimaryFaculty newFaculty = primaryFacultyRepository
                .findByFacultyId(newFacultyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Faculty not found.")
                );

        if (newFaculty.getStatus() != Status.ACTIVE) {
            throw new BadRequestException(
                    "Only an active faculty member can be assigned."
            );
        }

        String classDatabase = normalizeOptional(
                primaryClass.getLegacyDatabase()
        );
        String facultyDatabase = normalizeOptional(
                newFaculty.getLegacyDatabase()
        );
        if (classDatabase != null
                && !classDatabase.equalsIgnoreCase(facultyDatabase)) {
            throw new BadRequestException(
                    "The faculty member must belong to the same source database as the class."
            );
        }

        boolean changed = !Objects.equals(previousFacultyId, newFacultyId);
        if (changed) {
            primaryClass.setFacultyId(newFacultyId);
            primaryClassRepository.saveAndFlush(primaryClass);
            primaryClass.setFaculty(newFaculty);
        }

        return new ClassFacultyReassignmentResponse(
                toClassFacultyAssignmentResponse(primaryClass),
                previousFacultyId,
                newFacultyId,
                changed
        );
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboard", key = "'active'")
    public AdminDashboardResponse getDashboard() {
        DashboardTerm term = resolveDashboardTerm();

        return AdminDashboardResponse.builder()
                .summary(buildDashboardSummary(term))
                .programs(findDashboardProgramBreakdown(term))
                .facultyLoads(findDashboardFacultyLoads(term, 10))
                .build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboardSummary", key = "'active'")
    public AdminDashboardSummaryResponse getDashboardSummary() {
        return buildDashboardSummary(resolveDashboardTerm());
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

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboardPrograms", key = "'active'")
    public List<AdminDashboardProgramBreakdownResponse>
    getDashboardProgramBreakdown() {
        return findDashboardProgramBreakdown(resolveDashboardTerm());
    }

    private List<AdminDashboardProgramBreakdownResponse>
    findDashboardProgramBreakdown(DashboardTerm term) {
        return adminDashboardRepository
                .findProgramBreakdown(term.schoolYear(), term.semester())
                .stream()
                .map(this::toProgramBreakdownResponse)
                .toList();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(
            value = "adminDashboardFacultyLoads",
            key = "T(java.lang.Math).min(T(java.lang.Math).max(#limit, 1), 50)"
    )
    public List<AdminDashboardFacultyLoadResponse> getDashboardFacultyLoads(
            int limit
    ) {
        return findDashboardFacultyLoads(resolveDashboardTerm(), limit);
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public FacultyWorkloadCoverageResponse getFacultyWorkloadCoverage() {
        DashboardTerm term = resolveDashboardTerm();
        List<FacultyWorkloadCoverageFacultyResponse> rows =
                primaryFacultyRepository
                        .findFacultyWorkloadCoverage(
                                term.schoolYear(),
                                term.semester(),
                                term.workloadSemester(),
                                Status.ACTIVE.name(),
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

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public PageResponse<FetchFacultyResponse> facultyList(
            int page,
            int size,
            String search,
            String legacyDatabase
    ) {

        Page<PrimaryFaculty> facultyPage;

        Pageable pageable = PageRequest.of(page, size);
        String normalizedLegacyDatabase = normalizeLegacyDatabase(legacyDatabase);

        facultyPage = primaryFacultyRepository.searchExcludingCollege(
                search,
                College.FOR_MIGRATION,
                normalizedLegacyDatabase,
                pageable
        );

        List<FetchFacultyResponse> responseList = facultyPage.getContent().stream().map(faculty -> FetchFacultyResponse.builder().facultyId(faculty.getFacultyId()).firstname(faculty.getFirstname()).lastname(faculty.getLastname()).middlename(faculty.getMiddlename()).position(faculty.getPosition()).loadLimit(faculty.getLoadLimit() != null ? faculty.getLoadLimit().toString() : null).status(faculty.getStatus()).college(faculty.getCollege()).legacyDatabase(faculty.getLegacyDatabase()).build()).toList();

        return PageResponse.<FetchFacultyResponse>builder().content(responseList).page(facultyPage.getNumber()).size(facultyPage.getSize()).totalElements(facultyPage.getTotalElements()).totalPages(facultyPage.getTotalPages()).build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public PageResponse<FetchUserAccountsResponse> accountList(int page, int size) {

        Page<UserAccounts> userAccountsPage = userAccountsRepository.findAll(PageRequest.of(page, size));

        List<FetchUserAccountsResponse> responseList = userAccountsPage.getContent().stream().map(user -> FetchUserAccountsResponse.builder().userId(user.getUserId()).username(user.getUsername()).email(user.getEmail()).role(user.getRole()).status(user.getStatus()).college(user.getCollege()).programs(user.getPrograms()).majors(user.getMajors()).isEnabled(user.getIsEnabled()).isLocked(user.getIsLocked()).lastLoginAt(user.getLastLoginAt()).build()).toList();

        return PageResponse.<FetchUserAccountsResponse>builder().content(responseList).page(userAccountsPage.getNumber()).size(userAccountsPage.getSize()).totalElements(userAccountsPage.getTotalElements()).totalPages(userAccountsPage.getTotalPages()).build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "facultyEvaluationScores", key = "#page + ':' + #size")
    public PageResponse<FetchFacultyEvaluationScoreResponse> facultyEvaluationScore(int page, int size) {

        Page<FacultyEvaluationScore> scorePage = facultyEvaluationScoreRepository.findAllWithFaculty(PageRequest.of(page, size));

        List<FetchFacultyEvaluationScoreResponse> responseList = scorePage.getContent().stream().map(score -> {

            PrimaryFaculty faculty = score.getFaculty();

            return FetchFacultyEvaluationScoreResponse.builder().facultyEvaluationScoreId(score.getFacultyEvaluationScoreId()).facultyId(score.getFacultyId()).facultyName(faculty != null ? faculty.getFirstname() + " " + faculty.getLastname() : "N/A").position(faculty != null ? faculty.getPosition() : null).evaluatorId(score.getEvaluatorId()).classCode(score.getClassCode()).semester(score.getSemester()).schoolYear(String.valueOf(score.getSchoolYear())).subjectCode(score.getSubjectCode()).yearLevel(score.getYearLevel()).commentsOrFeedbacks(score.getCommentsOrFeedbacks()).overallAverageScore(score.getOverallAverageScore()).overallInterpretation(score.getOverallInterpretation()).build();
        }).toList();

        return PageResponse.<FetchFacultyEvaluationScoreResponse>builder().content(responseList).page(scorePage.getNumber()).size(scorePage.getSize()).totalElements(scorePage.getTotalElements()).totalPages(scorePage.getTotalPages()).build();
    }

    @CacheEvict(
            value = {
                    "faculties",
                    "facultyClasses",
                    "facultyEvaluationReports",
                    "adminDashboard",
                    "adminDashboardSummary",
                    "adminDashboardPrograms",
                    "adminDashboardFacultyLoads",
                    "facultyWorkloadCoverage",
                    "studentLoads",
                    "supervisorFacultyLoads",
                    "supervisorFacultyProgramLoads"
            },
            allEntries = true
    )
    public String updateFaculty(String facultyId, String firstname, String middlename, String lastname, String position, Double loadLimit, College college, Status status) {

        primaryFacultyRepository.findByFacultyId(facultyId).orElseThrow(() -> new ResourceNotFoundException("Faculty not found."));

        int updatedRows = primaryFacultyRepository.updateFaculty(facultyId, firstname, middlename, lastname, position, loadLimit, college, status);

        if (updatedRows > 0) {

            return "Faculty updated successfully.";
        }

        throw new BadRequestException("Failed to update faculty.");
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public PageResponse<FacultyWorkloadResponse> getFacultyWorkloads(
            int page,
            int size,
            String search,
            Integer schoolYear,
            String semester
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        Page<FacultyWorkload> workloadPage =
                facultyWorkloadRepository.searchWorkloads(
                        normalizeBlank(search),
                        schoolYear,
                        normalizeBlank(semester),
                        pageable
                );

        List<FacultyWorkloadResponse> content = workloadPage
                .getContent()
                .stream()
                .map(this::toFacultyWorkloadResponse)
                .toList();

        return PageResponse.<FacultyWorkloadResponse>builder()
                .content(content)
                .page(workloadPage.getNumber())
                .size(workloadPage.getSize())
                .totalElements(workloadPage.getTotalElements())
                .totalPages(workloadPage.getTotalPages())
                .build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager"
    )
    @CacheEvict(
            value = {
                    "adminDashboard",
                    "adminDashboardSummary",
                    "adminDashboardPrograms",
                    "adminDashboardFacultyLoads",
                    "facultyWorkloadCoverage",
                    "facultyWorkloadClassOptions",
                    "facultyEvaluationReports",
                    "supervisorFacultyLoads",
                    "supervisorFacultyProgramLoads"
            },
            allEntries = true
    )
    public FacultyWorkloadResponse upsertFacultyWorkload(
            FacultyWorkloadRequest request
    ) {
        validateFacultyWorkloadRequest(request);

        String facultyId = request.getFacultyId().trim();
        String semester = request.getSemester().trim();

        primaryFacultyRepository.findByFacultyId(facultyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Faculty not found.")
                );

        FacultyWorkload workload = resolveFacultyWorkloadForSave(
                request,
                facultyId,
                semester
        );

        workload.setFacultyId(facultyId);
        workload.setSchoolYear(request.getSchoolYear());
        workload.setSemester(semester);
        workload.setClassCode(normalizeBlank(request.getClassCode()));
        workload.setCourseCode(request.getCourseCode().trim());
        workload.setProgramCode(request.getProgramCode().trim());
        workload.setYearLevel(request.getYearLevel().trim());
        workload.setSectionCode(request.getSectionCode().trim());
        workload.setTotalHoursPerWeek(safeBigDecimal(request.getTotalHoursPerWeek()));
        workload.setTotalTeachingLoad(BigDecimal.ZERO);
        workload.setNumberOfPreparations(request.getNumberOfPreparations());
        workload.setDesignationEtu(safeBigDecimal(request.getDesignationEtu()));
        workload.setTotalWorkload(BigDecimal.ZERO);
        workload.setOverloadHours(
                request.getOverloadHours() == null
                        ? BigDecimal.ZERO
                        : request.getOverloadHours()
        );
        workload.setSource(
                request.getSource() == null
                        ? FacultyWorkloadSource.MANUAL
                        : request.getSource()
        );
        workload.setRemarks(normalizeBlank(request.getRemarks()));

        FacultyWorkload savedWorkload = facultyWorkloadRepository.save(workload);
        updateFacultyTermTeachingLoad(
                facultyId,
                request.getSchoolYear(),
                semester,
                request.getNumberOfPreparations(),
                request.getDesignationEtu()
        );

        return toFacultyWorkloadResponse(
                facultyWorkloadRepository
                        .findById(savedWorkload.getFacultyWorkloadId())
                        .orElse(savedWorkload)
        );
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public FacultyWorkloadResponse getFacultyWorkload(
            String facultyId,
            Integer schoolYear,
            String semester,
            String classCode,
            String courseCode,
            String programCode,
            String yearLevel,
            String sectionCode
    ) {
        if (facultyId == null || facultyId.trim().isEmpty()) {
            throw new BadRequestException("Faculty ID is required.");
        }

        if (schoolYear == null) {
            throw new BadRequestException("School year is required.");
        }

        if (semester == null || semester.trim().isEmpty()) {
            throw new BadRequestException("Semester is required.");
        }

        if (courseCode == null || courseCode.trim().isEmpty()) {
            throw new BadRequestException("Course code is required.");
        }

        if (programCode == null || programCode.trim().isEmpty()) {
            throw new BadRequestException("Program code is required.");
        }

        if (yearLevel == null || yearLevel.trim().isEmpty()) {
            throw new BadRequestException("Year level is required.");
        }

        if (sectionCode == null || sectionCode.trim().isEmpty()) {
            throw new BadRequestException("Section code is required.");
        }

        Optional<FacultyWorkload> workload =
                Optional.empty();

        String normalizedClassCode = normalizeBlank(classCode);
        if (normalizedClassCode != null) {
            workload = facultyWorkloadRepository
                    .findByFacultyIdAndSchoolYearAndSemesterAndClassCodeAndCourseCodeAndProgramCodeAndYearLevelAndSectionCode(
                            facultyId.trim(),
                            schoolYear,
                            semester.trim(),
                            normalizedClassCode,
                            courseCode.trim(),
                            programCode.trim(),
                            yearLevel.trim(),
                            sectionCode.trim()
                    );
        }

        FacultyWorkload resolvedWorkload = workload.or(() ->
                        facultyWorkloadRepository
                                .findByFacultyIdAndSchoolYearAndSemesterAndCourseCodeAndProgramCodeAndYearLevelAndSectionCode(
                                        facultyId.trim(),
                                        schoolYear,
                                        semester.trim(),
                                        courseCode.trim(),
                                        programCode.trim(),
                                        yearLevel.trim(),
                                        sectionCode.trim()
                                )
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Faculty workload not found."
                        )
                );

        return toFacultyWorkloadResponse(resolvedWorkload);
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public FacultyWorkloadResponse getFacultyWorkloadById(
            Long facultyWorkloadId
    ) {
        if (facultyWorkloadId == null) {
            throw new BadRequestException("Faculty workload ID is required.");
        }

        return toFacultyWorkloadResponse(
                facultyWorkloadRepository.findById(facultyWorkloadId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Faculty workload not found."
                                )
                        )
        );
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public List<FacultyWorkloadSectionOptionResponse>
    getFacultyWorkloadSectionOptions(
            Integer schoolYear,
            String semester
    ) {
        if (schoolYear == null) {
            throw new BadRequestException("School year is required.");
        }

        if (semester == null || semester.trim().isEmpty()) {
            throw new BadRequestException("Semester is required.");
        }

        return primarySectionRepository.findAvailableFacultyWorkloadSections(
                schoolYear,
                normalizeAcademicSemester(semester)
        );
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(
            value = "facultyWorkloadClassOptions",
            key = "T(java.util.Objects).toString(#facultyId, '').trim().toLowerCase() + ':' + #schoolYear + ':' + T(java.util.Objects).toString(#semester, '').trim().toUpperCase()"
    )
    public List<FacultyWorkloadClassOptionResponse>
    getFacultyWorkloadClassOptions(
            String facultyId,
            Integer schoolYear,
            String semester
    ) {
        if (facultyId == null || facultyId.trim().isEmpty()) {
            throw new BadRequestException("Faculty ID is required.");
        }

        if (schoolYear == null) {
            throw new BadRequestException("School year is required.");
        }

        if (semester == null || semester.trim().isEmpty()) {
            throw new BadRequestException("Semester is required.");
        }

        primaryFacultyRepository.findByFacultyId(facultyId.trim())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Faculty not found.")
                );

        return primaryClassRepository.findFacultyWorkloadClassOptionRows(
                        facultyId.trim(),
                        schoolYear,
                        normalizeAcademicSemester(semester)
                )
                .stream()
                .map(this::toFacultyWorkloadClassOptionResponse)
                .toList();
    }

    private FacultyWorkloadClassOptionResponse toFacultyWorkloadClassOptionResponse(Object[] row) {
        return new FacultyWorkloadClassOptionResponse(
                toStringValue(row[0]),
                toStringValue(row[1]),
                toIntegerValue(row[2]),
                toStringValue(row[3]),
                toStringValue(row[4]),
                toStringValue(row[5])
        );
    }

    private String toStringValue(Object value) {
        return value == null ? "" : value.toString();
    }

    private Integer toIntegerValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.valueOf(value.toString());
    }

    @Cacheable(
            value = "studentFacultyEvaluations",
            key = "(#search == null ? '' : #search.trim().toLowerCase()) + ':' + #page + ':' + #size + ':' + #sortBy + ':' + #sortDirection"
    )
    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public Page<StudentFacultyEvaluationDTO> getStudentFacultyEvaluationDetails(String search, int page, int size, String sortBy, String sortDirection) {

        Sort sort = sortDirection.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Object[]> rows = facultyEvaluationScoreRepository.findStudentFacultyEvaluationDetails(search, pageable);

        return rows.map(row -> new StudentFacultyEvaluationDTO(row[0] != null ? row[0].toString() : null, row[1] != null ? row[1].toString() : null, row[2] != null ? row[2].toString() : null, row[3] != null ? row[3].toString() : null, row[4] != null ? row[4].toString() : null, row[5] != null ? row[5].toString() : null, row[6] != null ? row[6].toString() : null, row[7] != null ? row[7].toString() : null, row[8] != null ? row[8].toString() : null, row[9] != null ? (LocalDateTime) row[9] : null));
    }

    @Transactional
    public String createUserAccount(CreateUserAccountDTO dto) {

        validateCreateUser(dto);

        if (userAccountsRepository.existsByUsername(dto.getUsername().trim())) {

            throw new BadRequestException("Username already exists.");
        }

        if (userAccountsRepository.existsByEmail(dto.getEmail().trim())) {

            throw new BadRequestException("Email already exists.");
        }

        UserAccounts user = UserAccounts.builder().username(dto.getUsername().trim()).email(dto.getEmail().trim().toLowerCase()).password(passwordEncoder.encode(dto.getPassword())).role(dto.getRole()).college(dto.getCollege()).programs(dto.getPrograms()).majors(dto.getMajors()).status(dto.getStatus()).isEnabled(true).isLocked(false).build();

        userAccountsRepository.save(user);

        return "User account created successfully.";
    }

    @Transactional
    public String updateUserAccount(UpdateUserAccountDTO dto) {

        validateUpdateUser(dto);

        UserAccounts existingUser = userAccountsRepository.findById(dto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found."));

        Optional<UserAccounts> usernameCheck = userAccountsRepository.findByUsername(dto.getUsername());

        if (usernameCheck.isPresent() && !usernameCheck.get().getUserId().equals(dto.getUserId())) {

            throw new BadRequestException("Username already exists.");
        }

        Optional<UserAccounts> emailCheck = userAccountsRepository.findByEmail(dto.getEmail());

        if (emailCheck.isPresent() && !emailCheck.get().getUserId().equals(dto.getUserId())) {

            throw new BadRequestException("Email already exists.");
        }

        existingUser.setUsername(dto.getUsername().trim());

        existingUser.setEmail(dto.getEmail().trim().toLowerCase());

        existingUser.setRole(dto.getRole());

        existingUser.setCollege(dto.getCollege());

        existingUser.setPrograms(dto.getPrograms());

        existingUser.setMajors(dto.getMajors());

        existingUser.setStatus(dto.getStatus());

        existingUser.setIsEnabled(dto.getIsEnabled());

        existingUser.setIsLocked(dto.getIsLocked());

        userAccountsRepository.save(existingUser);

        return "User account updated successfully.";
    }

    @Transactional
    public String updateUserPassword(UpdateUserPasswordDTO dto) {

        if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {

            throw new BadRequestException("Password is required.");
        }

        if (dto.getNewPassword().length() < 8) {

            throw new BadRequestException("Password must be at least 8 characters.");
        }

        UserAccounts user = userAccountsRepository.findById(dto.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        userAccountsRepository.save(user);

        return "Password updated successfully.";
    }

    @Cacheable(
            value = "studentSections",
            key = "(#programCode == null ? '' : #programCode.trim().toLowerCase()) + ':' + (#yearLevel == null ? '' : #yearLevel.trim().toLowerCase()) + ':' + (#sectionCode == null ? '' : #sectionCode.trim().toLowerCase()) + ':' + #page + ':' + #size"
    )
    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public PageResponse<StudentSectionDTO> getStudentSections(

            String programCode,

            String yearLevel,

            String sectionCode,

            int page,

            int size) {

        DashboardTerm term = resolveDashboardTerm();
        Pageable pageable = PageRequest.of(page, size);

        return PageMapper.toPageResponse(primarySectionRepository.getStudentSectionEvaluationData(
                programCode,
                yearLevel,
                sectionCode,
                term.schoolYear(),
                term.semester(),
                pageable
        ));
    }

    @Cacheable(
            value = "studentEvaluationStatus",
            key = "(#programCode == null ? '' : #programCode.trim().toLowerCase()) + ':' + (#yearLevel == null ? '' : #yearLevel.trim().toLowerCase()) + ':' + (#sectionCode == null ? '' : #sectionCode.trim().toLowerCase())"
    )
    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public List<StudentEvaluationStatusResponse> fetchStudentEvaluationStatus(

            String programCode,

            String yearLevel,

            String sectionCode) {

        if (programCode == null || programCode.trim().isEmpty()) {

            throw new BadRequestException("Program code is required.");
        }

        if (yearLevel == null || yearLevel.trim().isEmpty()) {

            throw new BadRequestException("Year level is required.");
        }

        if (sectionCode == null || sectionCode.trim().isEmpty()) {

            throw new BadRequestException("Section code is required.");
        }

        DashboardTerm term = resolveDashboardTerm();

        return primaryStudentLoadRepository.fetchStudentEvaluationStatus(

                programCode,

                yearLevel,

                sectionCode,

                term.schoolYear(),

                term.semester());
    }

    private void validateCreateUser(CreateUserAccountDTO dto) {

        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {

            throw new BadRequestException("Username is required.");
        }

        if (dto.getUsername().trim().length() < 4) {

            throw new BadRequestException("Username must be at least 4 characters.");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {

            throw new BadRequestException("Email is required.");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new BadRequestException("Invalid email address.");
        }

        if (dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {

            throw new BadRequestException("Password is required.");
        }

        if (dto.getPassword().length() < 8) {

            throw new BadRequestException("Password must be at least 8 characters.");
        }
    }

    private void validateUpdateUser(UpdateUserAccountDTO dto) {

        if (dto.getUserId() == null) {

            throw new BadRequestException("User ID is required.");
        }

        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {

            throw new BadRequestException("Username is required.");
        }

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {

            throw new BadRequestException("Email is required.");
        }

        if (!dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new BadRequestException("Invalid email address.");
        }
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

    private ClassFacultyAssignmentResponse toClassFacultyAssignmentResponse(
            PrimaryClass primaryClass
    ) {
        PrimaryFaculty faculty = primaryClass.getFaculty();

        return new ClassFacultyAssignmentResponse(
                primaryClass.getPrimaryClassId(),
                primaryClass.getClassCode(),
                primaryClass.getSubjectCode(),
                primaryClass.getSubject() == null
                        ? null
                        : primaryClass.getSubject().getDescriptiveTitle(),
                primaryClass.getSectionId(),
                primaryClass.getSection() == null
                        ? null
                        : primaryClass.getSection().getProgramCode(),
                primaryClass.getSection() == null
                        ? null
                        : primaryClass.getSection().getYearLevel(),
                primaryClass.getSection() == null
                        ? null
                        : primaryClass.getSection().getSectionCode(),
                primaryClass.getFacultyId(),
                faculty == null ? null : formatFacultyName(faculty),
                primaryClass.getSchoolYear(),
                primaryClass.getSemester(),
                primaryClass.getLegacyDatabase(),
                primaryClass.getSourceCampus() == null
                        ? null
                        : primaryClass.getSourceCampus().name()
        );
    }

    private FacultyAssignmentOptionResponse toFacultyAssignmentOptionResponse(
            PrimaryFaculty faculty
    ) {
        return new FacultyAssignmentOptionResponse(
                faculty.getFacultyId(),
                formatFacultyName(faculty),
                faculty.getPosition(),
                faculty.getCollege() == null
                        ? null
                        : faculty.getCollege().name(),
                faculty.getLegacyDatabase()
        );
    }

    private String formatFacultyName(PrimaryFaculty faculty) {
        String firstName = normalizeOptional(faculty.getFirstname());
        String middleName = normalizeOptional(faculty.getMiddlename());
        String lastName = normalizeOptional(faculty.getLastname());

        return String.join(
                " ",
                java.util.stream.Stream.of(
                                firstName,
                                middleName,
                                lastName
                        )
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
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
        College college = parseEnum(
                College.class,
                projection.getCollege()
        );
        Status status = parseEnum(
                Status.class,
                projection.getStatus()
        );

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

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private Double safeDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Double percentage(Long numerator, Long denominator) {
        if (denominator == null || denominator == 0L) {
            return 0.0;
        }

        return Math.round((numerator * 10000.0) / denominator) / 100.0;
    }

    private void validateFacultyWorkloadRequest(FacultyWorkloadRequest request) {
        if (request == null) {
            throw new BadRequestException("Faculty workload request is required.");
        }

        if (request.getFacultyId() == null || request.getFacultyId().trim().isEmpty()) {
            throw new BadRequestException("Faculty ID is required.");
        }

        if (request.getSchoolYear() == null) {
            throw new BadRequestException("School year is required.");
        }

        if (request.getSemester() == null || request.getSemester().trim().isEmpty()) {
            throw new BadRequestException("Semester is required.");
        }

        if (request.getCourseCode() == null || request.getCourseCode().trim().isEmpty()) {
            throw new BadRequestException("Course code is required.");
        }

        if (request.getProgramCode() == null || request.getProgramCode().trim().isEmpty()) {
            throw new BadRequestException("Program code is required.");
        }

        if (request.getYearLevel() == null || request.getYearLevel().trim().isEmpty()) {
            throw new BadRequestException("Year level is required.");
        }

        if (request.getSectionCode() == null || request.getSectionCode().trim().isEmpty()) {
            throw new BadRequestException("Section code is required.");
        }

        if (isNegative(request.getTotalTeachingLoad())) {
            throw new BadRequestException("Total teaching load cannot be negative.");
        }

        if (isNegative(request.getTotalHoursPerWeek())) {
            throw new BadRequestException("Total hours per week cannot be negative.");
        }

        if (request.getNumberOfPreparations() != null
                && request.getNumberOfPreparations() < 0) {
            throw new BadRequestException("Number of preparations cannot be negative.");
        }

        if (isNegative(request.getDesignationEtu())) {
            throw new BadRequestException("Designation ETU cannot be negative.");
        }

        if (isNegative(request.getTotalWorkload())) {
            throw new BadRequestException("Total workload cannot be negative.");
        }

        if (isNegative(request.getOverloadHours())) {
            throw new BadRequestException("Overload hours cannot be negative.");
        }
    }

    private FacultyWorkloadResponse toFacultyWorkloadResponse(
            FacultyWorkload workload
    ) {
        PrimaryFaculty faculty = workload.getFaculty();
        String facultyName = null;

        if (faculty != null) {
            facultyName = String.format(
                    "%s %s",
                    faculty.getFirstname() == null ? "" : faculty.getFirstname(),
                    faculty.getLastname() == null ? "" : faculty.getLastname()
            ).trim();
        }

        return FacultyWorkloadResponse.builder()
                .facultyWorkloadId(workload.getFacultyWorkloadId())
                .facultyId(workload.getFacultyId())
                .facultyName(facultyName == null || facultyName.isBlank()
                        ? workload.getFacultyId()
                        : facultyName)
                .college(faculty == null ? null : faculty.getCollege())
                .loadLimit(effectiveLoadLimit(workload.getNumberOfPreparations()).doubleValue())
                .schoolYear(workload.getSchoolYear())
                .semester(workload.getSemester())
                .classCode(workload.getClassCode())
                .courseCode(workload.getCourseCode())
                .programCode(workload.getProgramCode())
                .yearLevel(workload.getYearLevel())
                .sectionCode(workload.getSectionCode())
                .totalHoursPerWeek(workload.getTotalHoursPerWeek())
                .totalTeachingLoad(workload.getTotalTeachingLoad())
                .numberOfPreparations(workload.getNumberOfPreparations())
                .designationEtu(workload.getDesignationEtu())
                .totalWorkload(workload.getTotalWorkload())
                .overloadHours(workload.getOverloadHours())
                .loadStatus(workload.getLoadStatus())
                .source(workload.getSource())
                .remarks(workload.getRemarks())
                .build();
    }

    private FacultyWorkload resolveFacultyWorkloadForSave(
            FacultyWorkloadRequest request,
            String facultyId,
            String semester
    ) {
        if (request.getFacultyWorkloadId() != null) {
            return facultyWorkloadRepository
                    .findById(request.getFacultyWorkloadId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Faculty workload not found."
                            )
                    );
        }

        String classCode = normalizeBlank(request.getClassCode());

        if (classCode != null) {
            Optional<FacultyWorkload> workload =
                    facultyWorkloadRepository
                            .findByFacultyIdAndSchoolYearAndSemesterAndClassCodeAndCourseCodeAndProgramCodeAndYearLevelAndSectionCode(
                                    facultyId,
                                    request.getSchoolYear(),
                                    semester,
                                    classCode,
                                    request.getCourseCode().trim(),
                                    request.getProgramCode().trim(),
                                    request.getYearLevel().trim(),
                                    request.getSectionCode().trim()
                            );

            if (workload.isPresent()) {
                return workload.get();
            }
        }

        return facultyWorkloadRepository
                .findByFacultyIdAndSchoolYearAndSemesterAndCourseCodeAndProgramCodeAndYearLevelAndSectionCode(
                        facultyId,
                        request.getSchoolYear(),
                        semester,
                        request.getCourseCode().trim(),
                        request.getProgramCode().trim(),
                        request.getYearLevel().trim(),
                        request.getSectionCode().trim()
                )
                .orElseGet(FacultyWorkload::new);
    }

    private void updateFacultyTermTeachingLoad(
            String facultyId,
            Integer schoolYear,
            String semester,
            Integer numberOfPreparations,
            BigDecimal designationEtu
    ) {
        List<FacultyWorkload> termWorkloads =
                facultyWorkloadRepository.findAllByFacultyIdAndSchoolYearAndSemester(
                        facultyId,
                        schoolYear,
                        semester
                );

        BigDecimal totalTeachingLoad = termWorkloads
                .stream()
                .map(FacultyWorkload::getTotalHoursPerWeek)
                .map(this::safeBigDecimal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer effectiveNumberOfPreparations =
                safeNumberOfPreparations(numberOfPreparations);
        BigDecimal effectiveDesignationEtu = safeBigDecimal(designationEtu);
        BigDecimal loadLimit =
                effectiveLoadLimit(effectiveNumberOfPreparations);

        termWorkloads.forEach(termWorkload -> {
            BigDecimal totalWorkload =
                    totalTeachingLoad.add(effectiveDesignationEtu);
            BigDecimal overloadHours = totalWorkload.subtract(loadLimit);

            termWorkload.setNumberOfPreparations(effectiveNumberOfPreparations);
            termWorkload.setDesignationEtu(effectiveDesignationEtu);
            termWorkload.setTotalTeachingLoad(totalTeachingLoad);
            termWorkload.setTotalWorkload(totalWorkload);
            termWorkload.setOverloadHours(
                    overloadHours.compareTo(BigDecimal.ZERO) > 0
                            ? overloadHours
                            : BigDecimal.ZERO
            );
        });

        facultyWorkloadRepository.saveAll(termWorkloads);
    }

    private BigDecimal effectiveLoadLimit(Integer numberOfPreparations) {
        if (numberOfPreparations != null
                && numberOfPreparations >= HIGH_PREPARATION_THRESHOLD) {
            return HIGH_PREPARATION_LOAD_LIMIT;
        }

        return STANDARD_PREPARATION_LOAD_LIMIT;
    }

    private Integer safeNumberOfPreparations(Integer numberOfPreparations) {
        return numberOfPreparations == null
                ? 0
                : Math.max(numberOfPreparations, 0);
    }

    private BigDecimal safeBigDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }

    private String normalizeBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private String normalizeAcademicSemester(String value) {
        String normalized = value.trim();

        try {
            return Semester.valueOf(normalized).getValue();
        } catch (IllegalArgumentException ignored) {
            return normalized;
        }
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

    private record DashboardTerm(
            Integer schoolYear,
            String semester,
            String workloadSemester
    ) {
    }
}

