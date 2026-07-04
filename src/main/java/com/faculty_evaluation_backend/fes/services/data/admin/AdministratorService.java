package com.faculty_evaluation_backend.fes.services.data.admin;

import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownProjection;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardSummaryResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentEvaluationStatusResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentFacultyEvaluationDTO;
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
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.AdminDashboardRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdministratorService {

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
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final AdminDashboardRepository adminDashboardRepository;

    @Transactional
    @CacheEvict(
            value = {
                    "currentSchoolYearSemester",
                    "adminDashboard",
                    "adminDashboardSummary",
                    "adminDashboardPrograms",
                    "adminDashboardFacultyLoads",
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

    @Cacheable(value = "currentSchoolYearSemester", key = "'active'")
    public SchoolYearAndSemesterDTO fetchCurrentSchoolYearAndSemester() {

        SchoolYearAndSemester data = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).orElseThrow(() -> new ResourceNotFoundException("No active semester found."));

        return SchoolYearAndSemesterDTO.builder().id(data.getId()).schoolYear(data.getSchoolYear()).semester(data.getSemester()).status(data.getStatus()).createdAt(data.getCreatedAt()).build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboard", key = "'active'")
    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .summary(getDashboardSummary())
                .programs(getDashboardProgramBreakdown())
                .facultyLoads(getDashboardFacultyLoads(10))
                .build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboardSummary", key = "'active'")
    public AdminDashboardSummaryResponse getDashboardSummary() {
        DashboardTerm term = resolveDashboardTerm();

        Long totalStudents =
                safeLong(adminDashboardRepository.countDistinctStudentsByTerm(
                        term.schoolYear(),
                        term.semester()
                ));
        Long expectedEvaluations =
                safeLong(adminDashboardRepository.countExpectedEvaluationsByTerm(
                        term.schoolYear(),
                        term.semester()
                ));
        Long completedEvaluations =
                safeLong(adminDashboardRepository.countCompletedEvaluationsByTerm(
                        term.schoolYear(),
                        term.semester()
                ));

        return AdminDashboardSummaryResponse.builder()
                .schoolYear(term.schoolYear())
                .semester(term.semester())
                .totalStudents(totalStudents)
                .totalFaculty(safeLong(
                        adminDashboardRepository.countFacultyByTerm(
                                term.schoolYear(),
                                term.semester()
                        )
                ))
                .totalClasses(safeLong(
                        adminDashboardRepository.countClassesByTerm(
                                term.schoolYear(),
                                term.semester()
                        )
                ))
                .totalSubjects(safeLong(
                        adminDashboardRepository.countSubjectsByTerm(
                                term.schoolYear(),
                                term.semester()
                        )
                ))
                .totalPrograms(safeLong(
                        adminDashboardRepository.countProgramsByTerm(
                                term.schoolYear(),
                                term.semester()
                        )
                ))
                .totalSections(safeLong(
                        adminDashboardRepository.countSectionsByTerm(
                                term.schoolYear(),
                                term.semester()
                        )
                ))
                .expectedEvaluations(expectedEvaluations)
                .completedEvaluations(completedEvaluations)
                .evaluatedStudents(safeLong(
                        adminDashboardRepository.countEvaluatedStudentsByTerm(
                                term.schoolYear(),
                                term.semester()
                        )
                ))
                .pendingEvaluations(Math.max(
                        expectedEvaluations - completedEvaluations,
                        0
                ))
                .evaluationCompletionRate(
                        percentage(completedEvaluations, expectedEvaluations)
                )
                .averageOverallScore(safeDouble(
                        adminDashboardRepository.averageOverallScoreByTerm(
                                term.schoolYear(),
                                term.semester()
                        )
                ))
                .build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(value = "adminDashboardPrograms", key = "'active'")
    public List<AdminDashboardProgramBreakdownResponse>
    getDashboardProgramBreakdown() {
        DashboardTerm term = resolveDashboardTerm();

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
    @Cacheable(value = "adminDashboardFacultyLoads", key = "#limit")
    public List<AdminDashboardFacultyLoadResponse> getDashboardFacultyLoads(
            int limit
    ) {
        DashboardTerm term = resolveDashboardTerm();
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

    public PageResponse<FetchUserAccountsResponse> accountList(int page, int size) {

        Page<UserAccounts> userAccountsPage = userAccountsRepository.findAll(PageRequest.of(page, size));

        List<FetchUserAccountsResponse> responseList = userAccountsPage.getContent().stream().map(user -> FetchUserAccountsResponse.builder().userId(user.getUserId()).username(user.getUsername()).email(user.getEmail()).role(user.getRole()).status(user.getStatus()).college(user.getCollege()).programs(user.getPrograms()).majors(user.getMajors()).isEnabled(user.getIsEnabled()).isLocked(user.getIsLocked()).lastLoginAt(user.getLastLoginAt()).build()).toList();

        return PageResponse.<FetchUserAccountsResponse>builder().content(responseList).page(userAccountsPage.getNumber()).size(userAccountsPage.getSize()).totalElements(userAccountsPage.getTotalElements()).totalPages(userAccountsPage.getTotalPages()).build();
    }

    @Cacheable(value = "facultyEvaluationScores", key = "#page + ':' + #size")
    public PageResponse<FetchFacultyEvaluationScoreResponse> facultyEvaluationScore(int page, int size) {

        Page<FacultyEvaluationScore> scorePage = facultyEvaluationScoreRepository.findAllWithFaculty(PageRequest.of(page, size));

        List<FetchFacultyEvaluationScoreResponse> responseList = scorePage.getContent().stream().map(score -> {

            PrimaryFaculty faculty = score.getFaculty();

            return FetchFacultyEvaluationScoreResponse.builder().facultyEvaluationScoreId(score.getFacultyEvaluationScoreId()).facultyId(score.getFacultyId()).facultyName(faculty != null ? faculty.getFirstname() + " " + faculty.getLastname() : "N/A").position(score.getFaculty().getPosition()).evaluatorId(score.getEvaluatorId()).classCode(score.getClassCode()).semester(score.getSemester()).schoolYear(String.valueOf(score.getSchoolYear())).subjectCode(score.getSubjectCode()).yearLevel(score.getYearLevel()).commentsOrFeedbacks(score.getCommentsOrFeedbacks()).overallAverageScore(score.getOverallAverageScore()).overallInterpretation(score.getOverallInterpretation()).build();
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

    @Cacheable(
            value = "studentFacultyEvaluations",
            key = "(#search == null ? '' : #search.trim().toLowerCase()) + ':' + #page + ':' + #size + ':' + #sortBy + ':' + #sortDirection"
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
    public PageResponse<StudentSectionDTO> getStudentSections(

            String programCode,

            String yearLevel,

            String sectionCode,

            int page,

            int size) {

        Pageable pageable = PageRequest.of(page, size);

        return PageMapper.toPageResponse(primarySectionRepository.getStudentSectionEvaluationData(programCode, yearLevel, sectionCode, pageable));
    }

    @Cacheable(
            value = "studentEvaluationStatus",
            key = "(#programCode == null ? '' : #programCode.trim().toLowerCase()) + ':' + (#yearLevel == null ? '' : #yearLevel.trim().toLowerCase()) + ':' + (#sectionCode == null ? '' : #sectionCode.trim().toLowerCase())"
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

        return primaryStudentLoadRepository.fetchStudentEvaluationStatus(

                programCode,

                yearLevel,

                sectionCode);
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
                activeTerm.getSemester().getValue()
        );
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

    private record DashboardTerm(Integer schoolYear, String semester) {
    }
}

