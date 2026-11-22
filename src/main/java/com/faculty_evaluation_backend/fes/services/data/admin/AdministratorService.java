package com.faculty_evaluation_backend.fes.services.data.admin;

import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardSummaryResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyEvaluationReadinessPageResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.SupervisorEvaluationDashboardPageResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentEvaluationStatusResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentFacultyEvaluationDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentFacultyEvaluationProjection;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyWorkloadClassOptionResponse;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassHoursProjection;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultySubjectLoadProjection;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyWorkloadClassOptionProjection;
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
import com.faculty_evaluation_backend.fes.dto.student.StudentSectionEvaluationProjection;
import com.faculty_evaluation_backend.fes.dto.student.StudentSectionDTO;
import com.faculty_evaluation_backend.fes.dto.user_accounts.CreateUserAccountDTO;
import com.faculty_evaluation_backend.fes.dto.user_accounts.UpdateUserAccountDTO;
import com.faculty_evaluation_backend.fes.dto.user_accounts.UpdateUserPasswordDTO;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.primary.FacultyWorkload;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.FacultyWorkloadSource;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.FacultyWorkloadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimarySectionRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.utilities.mapper.PageMapper;
import com.faculty_evaluation_backend.fes.utilities.normalization.SemesterNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private static final String REGULAR_LOAD_STATUS = "Regular";
    private static final String OVERLOAD_LOAD_STATUS = "Overload";
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_EVALUATION_PAGE_SIZE = 50;
    private static final Set<String> STUDENT_EVALUATION_SORT_COLUMNS = Set.of(
            "student_id",
            "student_firstname",
            "student_lastname",
            "class_code",
            "program_code",
            "section_code",
            "faculty_id",
            "firstname",
            "lastname",
            "created_at"
    );

    private static final Set<String> ALLOWED_LEGACY_DATABASES = Set.of(
            "LEGACY_TALISAY",
            "LEGACY_ALIJIS",
            "LEGACY_FT",
            "LEGACY_BINALBAGAN"
    );
    private static final Set<String> SUPERVISOR_EVALUATION_STATUS_FILTERS =
            Set.of("EVALUATED", "PENDING");

    private final PrimaryFacultyRepository primaryFacultyRepository;

    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;

    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;

    private final PrimarySectionRepository primarySectionRepository;
    private final PrimaryClassRepository primaryClassRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final FacultyWorkloadRepository facultyWorkloadRepository;
    private final AdminClassAssignmentService adminClassAssignmentService;
    private final AdminDashboardService adminDashboardService;
    private final AdminUserAccountService adminUserAccountService;

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
        return adminClassAssignmentService.getClassAssignments(
                page,
                size,
                search,
                legacyDatabase
        );
    }

    @Transactional(readOnly = true)
    public List<FacultyAssignmentOptionResponse>
    getClassAssignmentFacultyOptions(String legacyDatabase) {
        return adminClassAssignmentService
                .getClassAssignmentFacultyOptions(legacyDatabase);
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
        return adminClassAssignmentService.reassignClassFaculty(
                primaryClassId,
                request
        );
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboard", key = "'active'")
    public AdminDashboardResponse getDashboard() {
        return adminDashboardService.getDashboard();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboardSummary", key = "'active'")
    public AdminDashboardSummaryResponse getDashboardSummary() {
        return adminDashboardService.getDashboardSummary();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboardPrograms", key = "'active'")
    public List<AdminDashboardProgramBreakdownResponse>
    getDashboardProgramBreakdown() {
        return adminDashboardService.getDashboardProgramBreakdown();
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
        return adminDashboardService.getDashboardFacultyLoads(limit);
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public SupervisorEvaluationDashboardPageResponse
    getSupervisorEvaluationDashboard(
            int page,
            int size,
            String search,
            String evaluationStatus,
            String legacyDatabase,
            String campus,
            Integer schoolYear,
            String semester
    ) {
        return adminDashboardService.getSupervisorEvaluationDashboard(
                page,
                size,
                search,
                evaluationStatus,
                legacyDatabase,
                campus,
                schoolYear,
                semester
        );
    }


    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "facultyEvaluationReadiness")
    public FacultyEvaluationReadinessPageResponse
    getFacultyEvaluationReadiness(
            int page,
            int size,
            String search,
            College college,
            String legacyDatabase,
            String campus,
            Integer schoolYear,
            String semester
    ) {
        log.info(
                "Faculty evaluation readiness cache miss | page={} size={} search={} college={} legacyDatabase={} campus={} schoolYear={} semester={}",
                page,
                size,
                search,
                college,
                legacyDatabase,
                campus,
                schoolYear,
                semester
        );

        return adminDashboardService.getFacultyEvaluationReadiness(
                page,
                size,
                search,
                college,
                legacyDatabase,
                campus,
                schoolYear,
                semester
        );
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "facultyEvaluationReadinessFacultyIds")
    public List<String> findFacultyEvaluationReadinessFacultyIds(
            String search,
            College college,
            String legacyDatabase,
            String campus,
            Integer schoolYear,
            String semester
    ) {
        log.info(
                "Faculty evaluation readiness faculty IDs cache miss | search={} college={} legacyDatabase={} campus={} schoolYear={} semester={}",
                search,
                college,
                legacyDatabase,
                campus,
                schoolYear,
                semester
        );

        return adminDashboardService.findFacultyEvaluationReadinessFacultyIds(
                search,
                college,
                legacyDatabase,
                campus,
                schoolYear,
                semester
        );
    }
    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "facultyWorkloadCoverage", key = "'active'")
    public FacultyWorkloadCoverageResponse getFacultyWorkloadCoverage() {
        return adminDashboardService.getFacultyWorkloadCoverage();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public PageResponse<FetchFacultyResponse> facultyList(
            int page,
            int size,
            String search,
            String legacyDatabase,
            College college,
            Status status
    ) {

        Page<PrimaryFaculty> facultyPage;

        Pageable pageable = PageRequest.of(
                safePage(page),
                safePageSize(size),
                Sort.by(
                        Sort.Order.asc("lastname"),
                        Sort.Order.asc("firstname"),
                        Sort.Order.asc("facultyId")
                )
        );
        String normalizedSearch = normalizeOptional(search);
        String normalizedLegacyDatabase = normalizeLegacyDatabase(legacyDatabase);

        facultyPage = primaryFacultyRepository.searchFaculties(
                normalizedSearch,
                college,
                status,
                normalizedLegacyDatabase,
                pageable
        );

        List<FetchFacultyResponse> responseList = facultyPage.getContent().stream().map(faculty -> FetchFacultyResponse.builder()
                .facultyId(faculty.getFacultyId())
                .firstname(faculty.getFirstname())
                .lastname(faculty.getLastname())
                .middlename(faculty.getMiddlename())
                .position(faculty.getPosition())
                .loadLimit(faculty.getLoadLimit() != null ? faculty.getLoadLimit().toString() : null)
                .status(faculty.getStatus())
                .college(faculty.getCollege())
                .legacyDatabase(faculty.getLegacyDatabase())
                .sourceCampus(faculty.getSourceCampus())
                .campus(faculty.getCampus())
                .build()).toList();

        return PageResponse.<FetchFacultyResponse>builder()
                .content(responseList)
                .page(facultyPage.getNumber())
                .size(facultyPage.getSize())
                .totalElements(facultyPage.getTotalElements())
                .totalPages(facultyPage.getTotalPages())
                .build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public PageResponse<FetchFacultyResponse> facultyList(
            int page,
            int size,
            String search,
            String legacyDatabase,
            College college
    ) {
        return facultyList(page, size, search, legacyDatabase, college, null);
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
        return facultyList(page, size, search, legacyDatabase, null, null);
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public PageResponse<FetchUserAccountsResponse> accountList(int page, int size, String search) {
        return adminUserAccountService.accountList(page, size, search);
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "facultyEvaluationScores", key = "#page + ':' + #size")
    public PageResponse<FetchFacultyEvaluationScoreResponse> facultyEvaluationScore(int page, int size) {

        Page<FacultyEvaluationScore> scorePage = facultyEvaluationScoreRepository.findAllWithFaculty(
                PageRequest.of(
                        safePage(page),
                        safePageSize(size),
                        Sort.by(Sort.Order.desc("createdAt"))
                )
        );

        List<FetchFacultyEvaluationScoreResponse> responseList = scorePage.getContent().stream().map(score -> {

            PrimaryFaculty faculty = score.getFaculty();

            return FetchFacultyEvaluationScoreResponse.builder().facultyEvaluationScoreId(score.getFacultyEvaluationScoreId()).facultyId(score.getFacultyId()).facultyName(facultyDisplayName(faculty)).position(faculty != null ? faculty.getPosition() : null).evaluatorId(score.getEvaluatorId()).classCode(score.getClassCode()).semester(score.getSemester()).schoolYear(String.valueOf(score.getSchoolYear())).subjectCode(score.getSubjectCode()).yearLevel(score.getYearLevel()).commentsOrFeedbacks(score.getCommentsOrFeedbacks()).overallAverageScore(score.getOverallAverageScore()).overallInterpretation(score.getOverallInterpretation()).build();
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
    @Transactional(transactionManager = "primaryTransactionManager")
    public String updateFaculty(
            String facultyId,
            String firstname,
            String middlename,
            String lastname,
            String position,
            Double loadLimit,
            College college,
            Status status,
            LegacyDatabase sourceCampus,
            String legacyDatabase
    ) {
        PrimaryFaculty faculty = primaryFacultyRepository.findByFacultyId(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found."));

        faculty.setFirstname(firstname);
        faculty.setMiddlename(middlename);
        faculty.setLastname(lastname);
        faculty.setPosition(position);
        faculty.setLoadLimit(loadLimit);
        faculty.setCollege(college);
        faculty.setStatus(status);

        if (sourceCampus != null) {
            faculty.setSourceCampus(sourceCampus);
            if (legacyDatabase == null || legacyDatabase.isBlank()) {
                faculty.setLegacyDatabase(sourceCampus.name());
            }
        }

        if (legacyDatabase != null && !legacyDatabase.isBlank()) {
            String trimmedDb = legacyDatabase.trim();
            faculty.setLegacyDatabase(trimmedDb);
            if (sourceCampus == null) {
                try {
                    faculty.setSourceCampus(LegacyDatabase.valueOf(trimmedDb.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        String dbName = faculty.getLegacyDatabase() != null ? faculty.getLegacyDatabase() :
                (faculty.getSourceCampus() != null ? faculty.getSourceCampus().name() : null);

        if (dbName != null) {
            String campus = switch (dbName.toUpperCase()) {
                case "LEGACY_TALISAY", "TALISAY" -> "TALISAY";
                case "LEGACY_ALIJIS", "ALIJIS" -> "ALIJIS";
                case "LEGACY_BINALBAGAN", "BINALBAGAN" -> "BINALBAGAN";
                case "LEGACY_FT", "FT" -> "FT";
                default -> faculty.getCampus();
            };
            faculty.setCampus(campus);
        }

        primaryFacultyRepository.save(faculty);

        return "Faculty updated successfully.";
    }

    public String updateFaculty(String facultyId, String firstname, String middlename, String lastname, String position, Double loadLimit, College college, Status status) {
        return updateFaculty(facultyId, firstname, middlename, lastname, position, loadLimit, college, status, null, null);
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
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<FacultyWorkload> workloadPage =
                facultyWorkloadRepository.searchWorkloads(
                        normalizeBlank(search),
                        schoolYear,
                        normalizeSemester(semester),
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
        String semester = normalizeSemester(request.getSemester());

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
        BigDecimal hoursPerWeek = safeBigDecimal(request.getTotalHoursPerWeek());
        if (hoursPerWeek.compareTo(BigDecimal.ZERO) == 0) {
            var classHours = primaryClassRepository.findClassSubjectHours(
                    facultyId,
                    normalizeBlank(request.getClassCode()),
                    request.getCourseCode().trim()
            );
            if (classHours.isPresent() && classHours.get().getTotalHoursPerWeek() != null) {
                hoursPerWeek = BigDecimal.valueOf(classHours.get().getTotalHoursPerWeek());
            }
        }
        workload.setTotalHoursPerWeek(hoursPerWeek);
        workload.setTotalTeachingLoad(BigDecimal.ZERO);
        workload.setNumberOfPreparations(request.getNumberOfPreparations());
        workload.setDesignationEtu(safeBigDecimal(request.getDesignationEtu()));
        workload.setTotalWorkload(BigDecimal.ZERO);
        workload.setOverloadHours(
                request.getOverloadHours() == null
                        ? BigDecimal.ZERO
                        : request.getOverloadHours()
        );
        workload.setLoadStatus(normalizeLoadStatus(request.getLoadStatus()));
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
                    .findByFacultyClassAndEquivalentSemester(
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
                                .findByFacultyCourseSectionAndEquivalentSemester(
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
    public void deleteFacultyWorkload(Long facultyWorkloadId) {
        if (facultyWorkloadId == null) {
            throw new BadRequestException("Faculty workload ID is required.");
        }

        FacultyWorkload workload = facultyWorkloadRepository
                .findById(facultyWorkloadId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Faculty workload not found."
                        )
                );
        String facultyId = workload.getFacultyId();
        Integer schoolYear = workload.getSchoolYear();
        String semester = workload.getSemester();
        Integer numberOfPreparations =
                workload.getNumberOfPreparations();
        BigDecimal designationEtu = workload.getDesignationEtu();

        facultyWorkloadRepository.delete(workload);
        facultyWorkloadRepository.flush();

        updateFacultyTermTeachingLoad(
                facultyId,
                schoolYear,
                semester,
                numberOfPreparations,
                designationEtu
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
                normalizeSemester(semester)
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
                        normalizeSemester(semester)
                )
                .stream()
                .map(this::toFacultyWorkloadClassOptionResponse)
                .toList();
    }

    private FacultyWorkloadClassOptionResponse toFacultyWorkloadClassOptionResponse(
            FacultyWorkloadClassOptionProjection row
    ) {
        return new FacultyWorkloadClassOptionResponse(
                emptyIfNull(row.getClassCode()),
                emptyIfNull(row.getCourseCode()),
                row.getSectionId(),
                emptyIfNull(row.getProgramCode()),
                emptyIfNull(row.getYearLevel()),
                emptyIfNull(row.getSectionCode()),
                row.getLecUnits() != null ? row.getLecUnits() : 0.0,
                row.getLabUnits() != null ? row.getLabUnits() : 0.0,
                row.getLecHours() != null ? row.getLecHours() : 0.0,
                row.getLabHours() != null ? row.getLabHours() : 0.0,
                row.getTotalHoursPerWeek() != null
                        ? BigDecimal.valueOf(row.getTotalHoursPerWeek())
                        : BigDecimal.ZERO,
                row.getTotalTeachingLoad() != null
                        ? BigDecimal.valueOf(row.getTotalTeachingLoad())
                        : BigDecimal.ZERO
        );
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
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

        Sort sort = safeStudentEvaluationSort(sortBy, sortDirection);

        Pageable pageable = PageRequest.of(
                safePage(page),
                Math.min(safePageSize(size), MAX_EVALUATION_PAGE_SIZE),
                sort
        );

        Page<StudentFacultyEvaluationProjection> rows =
                facultyEvaluationScoreRepository
                        .findStudentFacultyEvaluationDetails(
                                normalizeOptional(search),
                                pageable
                        );

        return rows.map(this::toStudentFacultyEvaluationDTO);
    }

    @Transactional
    public String createUserAccount(CreateUserAccountDTO dto) {
        return adminUserAccountService.createUserAccount(dto);
    }

    @Transactional
    public String updateUserAccount(UpdateUserAccountDTO dto) {
        return adminUserAccountService.updateUserAccount(dto);
    }

    @Transactional
    public String updateUserPassword(UpdateUserPasswordDTO dto) {
        return adminUserAccountService.updateUserPassword(dto);
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
        Pageable pageable = PageRequest.of(safePage(page), safePageSize(size));

        Page<StudentSectionDTO> sectionPage =
                primarySectionRepository.getStudentSectionEvaluationData(
                                normalizeOptional(programCode),
                                normalizeOptional(yearLevel),
                                normalizeOptional(sectionCode),
                                term.schoolYear(),
                                term.semester(),
                                pageable
                        )
                        .map(this::toStudentSectionDTO);

        return PageMapper.toPageResponse(sectionPage);
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
                term.semester()
        ).stream()
         .map(p -> StudentEvaluationStatusResponse.builder()
                 .studentId(p.getStudentId())
                 .programCode(p.getProgramCode())
                 .yearLevel(p.getYearLevel())
                 .sectionCode(p.getSectionCode())
                 .subjectCode(p.getSubjectCode())
                 .createdAt(p.getCreatedAt())
                 .evaluationStatus(p.getEvaluationStatus())
                 .build())
         .toList();
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

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private String facultyDisplayName(PrimaryFaculty faculty) {
        if (faculty == null) {
            return "N/A";
        }

        String lastname = normalizeOptional(faculty.getLastname());
        String firstname = normalizeOptional(faculty.getFirstname());
        String middlename = middleNameOrInitial(faculty.getMiddlename());
        StringBuilder givenName = new StringBuilder();

        appendNamePart(givenName, firstname);
        appendNamePart(givenName, middlename);

        if (lastname != null && !givenName.isEmpty()) {
            return lastname + ", " + givenName;
        }

        if (lastname != null) {
            return lastname;
        }

        if (!givenName.isEmpty()) {
            return givenName.toString();
        }

        String facultyId = normalizeOptional(faculty.getFacultyId());
        return facultyId == null ? "N/A" : facultyId;
    }

    private String middleNameOrInitial(String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return null;
        }

        return normalized.length() == 1
                ? normalized + "."
                : normalized;
    }

    private void appendNamePart(StringBuilder displayName, String value) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            return;
        }

        if (!displayName.isEmpty()) {
            displayName.append(' ');
        }
        displayName.append(normalized);
    }

    private Long safeLong(Long value) {
        return value == null ? 0L : value;
    }

    private StudentFacultyEvaluationDTO toStudentFacultyEvaluationDTO(
            StudentFacultyEvaluationProjection row
    ) {
        return new StudentFacultyEvaluationDTO(
                row.getStudentId(),
                row.getStudentFirstname(),
                row.getStudentLastname(),
                row.getClassCode(),
                row.getProgramCode(),
                row.getSectionCode(),
                row.getFacultyId(),
                row.getFacultyFirstname(),
                row.getFacultyLastname(),
                row.getCreatedAt()
        );
    }

    private StudentSectionDTO toStudentSectionDTO(
            StudentSectionEvaluationProjection row
    ) {
        return new StudentSectionDTO(
                row.getProgramCode(),
                row.getYearLevel(),
                row.getSectionCode(),
                safeLong(row.getTotalStudents()),
                safeLong(row.getEvaluatedStudents()),
                safeLong(row.getNotYetEvaluated())
        );
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

        normalizeLoadStatus(request.getLoadStatus());
    }

    private String normalizeLoadStatus(String loadStatus) {
        if (loadStatus == null || loadStatus.trim().isEmpty()) {
            return REGULAR_LOAD_STATUS;
        }

        String normalized = loadStatus.trim();

        if (normalized.equalsIgnoreCase(REGULAR_LOAD_STATUS)
                || normalized.equalsIgnoreCase("REGULAR_LOAD")
                || normalized.equalsIgnoreCase("REGULAR")) {
            return REGULAR_LOAD_STATUS;
        }

        if (normalized.equalsIgnoreCase(OVERLOAD_LOAD_STATUS)) {
            return OVERLOAD_LOAD_STATUS;
        }

        throw new BadRequestException(
                "Load status must be Regular or Overload."
        );
    }

    private FacultyWorkloadResponse toFacultyWorkloadResponse(
            FacultyWorkload workload
    ) {
        PrimaryFaculty faculty = workload.getFaculty();
        String facultyName = null;

        if (faculty != null) {
            facultyName = facultyDisplayName(faculty);
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
                            .findByFacultyClassAndEquivalentSemester(
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
                .findByFacultyCourseSectionAndEquivalentSemester(
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
                facultyWorkloadRepository.findAllByFacultyIdAndSchoolYearAndEquivalentSemester(
                        facultyId,
                        schoolYear,
                        semester
                );

        // Map through primary_class and primary_subject to get accurate teaching load from units
        BigDecimal calculatedTeachingLoad = primaryClassRepository.calculateFacultySubjectTotals(
                facultyId,
                schoolYear,
                semester
        ).map(FacultySubjectLoadProjection::getTotalTeachingLoad)
         .filter(Objects::nonNull)
         .map(BigDecimal::valueOf)
         .orElse(BigDecimal.ZERO);

        BigDecimal totalTeachingLoad = calculatedTeachingLoad.compareTo(BigDecimal.ZERO) > 0
                ? calculatedTeachingLoad
                : termWorkloads
                        .stream()
                        .map(FacultyWorkload::getTotalHoursPerWeek)
                        .map(this::safeBigDecimal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ensure any termWorkload with 0 totalHoursPerWeek is populated from primary_class + primary_subject
        termWorkloads.forEach(tw -> {
            if (tw.getTotalHoursPerWeek() == null || tw.getTotalHoursPerWeek().compareTo(BigDecimal.ZERO) == 0) {
                primaryClassRepository.findClassSubjectHours(facultyId, tw.getClassCode(), tw.getCourseCode())
                        .ifPresent(ch -> {
                            if (ch.getTotalHoursPerWeek() != null && ch.getTotalHoursPerWeek() > 0) {
                                tw.setTotalHoursPerWeek(BigDecimal.valueOf(ch.getTotalHoursPerWeek()));
                            }
                        });
            }
        });

        Integer effectiveNumberOfPreparations = numberOfPreparations;
        if (effectiveNumberOfPreparations == null) {
            effectiveNumberOfPreparations = termWorkloads.stream()
                    .map(FacultyWorkload::getNumberOfPreparations)
                    .filter(n -> n != null && n > 0)
                    .findFirst()
                    .orElseGet(() -> {
                        long distinctCourses = termWorkloads.stream()
                                .map(FacultyWorkload::getCourseCode)
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .distinct()
                                .count();
                        return (int) Math.max(distinctCourses, 1);
                    });
        }
        effectiveNumberOfPreparations = safeNumberOfPreparations(effectiveNumberOfPreparations);

        BigDecimal effectiveDesignationEtu = designationEtu;
        if (effectiveDesignationEtu == null) {
            effectiveDesignationEtu = termWorkloads.stream()
                    .map(FacultyWorkload::getDesignationEtu)
                    .filter(d -> d != null && d.compareTo(BigDecimal.ZERO) > 0)
                    .findFirst()
                    .orElse(BigDecimal.ZERO);
        }
        effectiveDesignationEtu = safeBigDecimal(effectiveDesignationEtu);

        BigDecimal loadLimit =
                effectiveLoadLimit(effectiveNumberOfPreparations);

        BigDecimal cumulativeLoad = BigDecimal.ZERO;
        for (FacultyWorkload termWorkload : termWorkloads) {
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

            BigDecimal classHours = safeBigDecimal(termWorkload.getTotalHoursPerWeek());
            cumulativeLoad = cumulativeLoad.add(classHours);
            if (cumulativeLoad.compareTo(loadLimit) > 0 && overloadHours.compareTo(BigDecimal.ZERO) > 0) {
                termWorkload.setLoadStatus("Overload");
            } else {
                termWorkload.setLoadStatus("Regular");
            }
        }

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

    private Sort safeStudentEvaluationSort(String sortBy, String sortDirection) {
        String normalizedSort = normalizeBlank(sortBy);

        if (normalizedSort == null
                || !STUDENT_EVALUATION_SORT_COLUMNS.contains(normalizedSort)) {
            normalizedSort = "created_at";
        }

        Sort.Direction direction =
                "asc".equalsIgnoreCase(normalizeBlank(sortDirection))
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return Sort.by(direction, normalizedSort);
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
    public Map<String, Object> autoPopulateAndSyncFacultyWorkloads(Integer schoolYear, String semester) {
        Integer resolvedYear = schoolYear;
        String resolvedSemester = semester;

        if (resolvedYear == null || resolvedSemester == null || resolvedSemester.isBlank()) {
            SchoolYearAndSemester activeTerm = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE)
                    .orElseThrow(() -> new ResourceNotFoundException("No active school year and semester found."));
            resolvedYear = activeTerm.getSchoolYear();
            resolvedSemester = activeTerm.getSemester().getValue();
        }

        final Integer targetYear = resolvedYear;
        final String canonicalSemester = normalizeSemester(resolvedSemester);

        log.info("Starting auto-population and synchronization of faculty workloads for year: {}, semester: {}",
                targetYear, canonicalSemester);

        // Step 1: Find all unpopulated class assignments in primary_class for this term
        List<FacultyWorkloadClassOptionProjection> unpopulated =
                primaryClassRepository.findUnpopulatedClassOptionsForTerm(targetYear, canonicalSemester);

        int createdCount = 0;
        Set<String> affectedFacultyIds = new HashSet<>();

        if (!unpopulated.isEmpty()) {
            List<FacultyWorkload> newWorkloads = new ArrayList<>();
            Map<String, BigDecimal> facultyDesignationCache = new HashMap<>();

            for (FacultyWorkloadClassOptionProjection opt : unpopulated) {
                if (opt.getFacultyId() == null || opt.getFacultyId().isBlank()) {
                    continue;
                }

                String facultyId = opt.getFacultyId().trim();
                FacultyWorkload fw = new FacultyWorkload();
                fw.setFacultyId(facultyId);
                fw.setSchoolYear(targetYear);
                fw.setSemester(canonicalSemester);
                fw.setClassCode(normalizeBlank(opt.getClassCode()));
                fw.setCourseCode(opt.getCourseCode() != null ? opt.getCourseCode().trim() : "");
                fw.setProgramCode(opt.getProgramCode() != null ? opt.getProgramCode().trim() : "");
                fw.setYearLevel(opt.getYearLevel() != null ? opt.getYearLevel().trim() : "");
                fw.setSectionCode(opt.getSectionCode() != null ? opt.getSectionCode().trim() : "");

                // Calculate hours: prefer explicit totalHours, then lec+lab formula (lec*1 + lab*3), fallback 3.0
                double hours = 0.0;
                if (opt.getTotalHoursPerWeek() != null && opt.getTotalHoursPerWeek() > 0) {
                    hours = opt.getTotalHoursPerWeek();
                } else if (opt.getLecHours() != null || opt.getLabHours() != null) {
                    hours = (opt.getLecHours() != null ? opt.getLecHours() : 0.0)
                            + (opt.getLabHours() != null ? opt.getLabHours() : 0.0);
                } else if (opt.getLecUnits() != null || opt.getLabUnits() != null) {
                    double lecU = opt.getLecUnits() != null ? opt.getLecUnits() : 0.0;
                    double labU = opt.getLabUnits() != null ? opt.getLabUnits() : 0.0;
                    hours = (lecU * 1.0) + (labU * 3.0);
                }
                if (hours <= 0.0) {
                    hours = 3.0; // standard collegiate course default
                }

                BigDecimal inheritedDesignation = facultyDesignationCache.computeIfAbsent(facultyId, fid -> {
                    List<FacultyWorkload> currentWorkloads = facultyWorkloadRepository
                            .findAllByFacultyIdAndSchoolYearAndEquivalentSemester(fid, targetYear, canonicalSemester);
                    for (FacultyWorkload existing : currentWorkloads) {
                        if (existing.getDesignationEtu() != null && existing.getDesignationEtu().compareTo(BigDecimal.ZERO) > 0) {
                            return existing.getDesignationEtu();
                        }
                    }
                    List<BigDecimal> priorEtus = facultyWorkloadRepository.findPriorDesignationEtus(
                            fid,
                            targetYear,
                            canonicalSemester,
                            PageRequest.of(0, 1)
                    );
                    return (priorEtus != null && !priorEtus.isEmpty() && priorEtus.get(0) != null)
                            ? priorEtus.get(0)
                            : BigDecimal.ZERO;
                });

                fw.setTotalHoursPerWeek(BigDecimal.valueOf(hours));
                fw.setTotalTeachingLoad(BigDecimal.ZERO);
                fw.setNumberOfPreparations(1);
                fw.setDesignationEtu(inheritedDesignation);
                fw.setTotalWorkload(BigDecimal.ZERO);
                fw.setOverloadHours(BigDecimal.ZERO);
                fw.setLoadStatus("Regular");
                fw.setSource(FacultyWorkloadSource.SYSTEM);

                newWorkloads.add(fw);
                affectedFacultyIds.add(fw.getFacultyId());
            }

            if (!newWorkloads.isEmpty()) {
                facultyWorkloadRepository.saveAll(newWorkloads);
                createdCount = newWorkloads.size();
                log.info("Auto-created {} new faculty workload assignments.", createdCount);
            }
        }

        // Step 2: Synchronize and repair hours for all existing workloads in this term
        List<FacultyWorkload> allWorkloads = facultyWorkloadRepository.searchWorkloads(
                null,
                targetYear,
                canonicalSemester,
                PageRequest.of(0, 10000)
        ).getContent();

        int updatedCount = 0;
        for (FacultyWorkload fw : allWorkloads) {
            affectedFacultyIds.add(fw.getFacultyId());

            if (fw.getTotalHoursPerWeek() == null || fw.getTotalHoursPerWeek().compareTo(BigDecimal.ZERO) == 0) {
                var classHours = primaryClassRepository.findClassSubjectHours(
                        fw.getFacultyId(),
                        fw.getClassCode(),
                        fw.getCourseCode()
                );
                if (classHours.isPresent() && classHours.get().getTotalHoursPerWeek() != null && classHours.get().getTotalHoursPerWeek() > 0) {
                    fw.setTotalHoursPerWeek(BigDecimal.valueOf(classHours.get().getTotalHoursPerWeek()));
                    updatedCount++;
                } else {
                    fw.setTotalHoursPerWeek(BigDecimal.valueOf(3.0));
                    updatedCount++;
                }
            }
        }

        // Step 3: Recalculate teaching load, preparations, overload hours and loadStatus for all affected faculty
        for (String facultyId : affectedFacultyIds) {
            updateFacultyTermTeachingLoad(
                    facultyId,
                    targetYear,
                    canonicalSemester,
                    null,
                    null
            );
        }

        log.info("Finished faculty workload auto-population. Created: {}, Updated: {}", createdCount, updatedCount);

        return Map.of(
                "status", "SUCCESS",
                "message", String.format("Auto-populated %d new assignments and synchronized %d workload records.",
                        createdCount, updatedCount),
                "createdCount", createdCount,
                "updatedCount", updatedCount
        );
    }

    public int syncFacultyWorkloadsFromPrimaryClasses(Integer schoolYear, String semester) {
        Map<String, Object> result = autoPopulateAndSyncFacultyWorkloads(schoolYear, semester);
        return (Integer) result.getOrDefault("createdCount", 0) + (Integer) result.getOrDefault("updatedCount", 0);
    }
}
