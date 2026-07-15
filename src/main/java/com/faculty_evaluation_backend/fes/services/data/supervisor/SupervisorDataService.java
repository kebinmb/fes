package com.faculty_evaluation_backend.fes.services.data.supervisor;

import com.faculty_evaluation_backend.fes.dto.dashboard.ClassStudentEvaluationStatsResponse;
import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.EvaluatedStudentsDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyLoadDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyProgramLoadsDTO;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.data.SchoolYearAndSemester;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryFaculty;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.EvaluationType;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Majors;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Programs;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.data.SchoolYearAndSemesterRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.FacultyWorkloadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryClassRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupervisorDataService {
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final PrimaryClassRepository primaryClassRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final SchoolYearAndSemesterRepository schoolYearAndSemesterRepository;
    private final UserAccountsRepository userAccountsRepository;
    private final FacultyWorkloadRepository facultyWorkloadRepository;

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    @Cacheable(
            value = "faculties",
            key = "(#college == null ? '' : #college.trim().toUpperCase()) + ':' + (#status == null ? '' : #status.trim().toUpperCase())"
    )
    public List<FacultyDTO> getFacultiesByCollegeAndStatus(String college, String status) {

        College collegeEnum = College.valueOf(college.toUpperCase());
        Status statusEnum = Status.valueOf(status.toUpperCase());

        log.info("Fetching faculties | college={} | status={}", collegeEnum, statusEnum);

        return primaryFacultyRepository.findByCollegeAndStatus(collegeEnum, statusEnum).stream().map(f -> new FacultyDTO(f.getFacultyId(), f.getLastname(), f.getFirstname(), f.getPosition(), f.getLoadLimit(), f.getMiddlename(), f.getCollege().name(), f.getStatus().name())).toList();
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    @Cacheable(
            value = "supervisorFacultyLoads",
            key = "#userId + ':' + (#search == null ? '' : #search.trim().toLowerCase()) + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public Page<FacultyLoadDTO> getFacultyLoadsByProgram(

            Long userId,

            String search,

            Pageable pageable) {

        log.info("Fetching faculty loads | userId={} | search={} | page={} | size={}", userId, search, pageable.getPageNumber(), pageable.getPageSize());

        userAccountsRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Page<FacultyLoadDTO> result = primaryClassRepository.findFacultyLoadsByProgram(userId, search, pageable);

        log.info("Faculty loads fetched successfully | totalElements={}", result.getTotalElements());

        return result;
    }

    private String extractSectionCode(Majors majors) {

        if (majors == null) {
            return null;
        }

        String value = majors.getDatabaseValue();

        if (value == null || value.isBlank() || value.equalsIgnoreCase("NONE")) {
            return null;
        }

        return value.trim();
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public List<FacultyClassDTO> findFacultyClassesForSupervisor(
            Long userId,
            String facultyId
    ) {

        SchoolYearAndSemester data = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).orElseThrow(() -> new RuntimeException("No active school year and semester found."));

        log.debug("Fetching classes | facultyId={} | schoolYear={} | semester={}", facultyId, data.getSchoolYear(), data.getSemester());

        UserAccounts supervisor =
                userAccountsRepository
                        .findById(userId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "User not found with id: " + userId
                                )
                        );

        String programCode = resolveProgramCode(supervisor);

        assertFacultyInSupervisorScope(userId, facultyId, data);

        List<FacultyClassDTO> result =
                primaryClassRepository.findFacultyClasses(
                        facultyId,
                        data.getSchoolYear(),
                        data.getSemester().getValue(),
                        programCode
                );

        log.info("Classes fetched | facultyId={} | count={}", facultyId, result.size());

        return result;
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public void assertFacultyInSupervisorScope(Long userId, String facultyId) {
        SchoolYearAndSemester data = schoolYearAndSemesterRepository.findByStatus(Status.ACTIVE).orElseThrow(() -> new RuntimeException("No active school year and semester found."));
        assertFacultyInSupervisorScope(userId, facultyId, data);
    }

    private void assertFacultyInSupervisorScope(
            Long userId,
            String facultyId,
            SchoolYearAndSemester data
    ) {
        boolean hasScopedClass = primaryClassRepository.countFacultyInSupervisorScope(
                userId,
                facultyId,
                data.getSchoolYear(),
                data.getSemester().getValue()
        ) > 0;

        if (hasScopedClass) {
            return;
        }

        boolean hasScopedWorkload =
                facultyWorkloadRepository.countFacultyWorkloadInSupervisorScope(
                        userId,
                        facultyId,
                        data.getSchoolYear(),
                        data.getSemester().getValue()
                ) > 0;

        if (!hasScopedWorkload) {
            throw new AccessDeniedException("Faculty is outside your assigned scope.");
        }
    }

    @Transactional(transactionManager = "primaryTransactionManager", readOnly = true)
    public boolean hasEvaluated(String facultyId, String evaluatorId, String classCode, String subjectCode, String yearLevel, String semester, Integer schoolYear) {
        return facultyEvaluationScoreRepository.existsByFacultyIdAndEvaluatorIdAndSubjectCodeAndSemesterAndSchoolYearAndEvaluationType(
                facultyId,
                evaluatorId,
                subjectCode,
                semester,
                schoolYear,
                EvaluationType.ROLE_PROGRAM_CHAIR
        );
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public Page<FacultyProgramLoadsDTO> getFacultyInPrograms(

            Long userId,

            String search,

            String campus,

            Pageable pageable
    ) {

        log.info(
                "Fetching faculty program loads | userId={} | search={} | campus={} | page={} | size={}",
                userId,
                search,
                campus,
                pageable.getPageNumber(),
            pageable.getPageSize()
        );

        SchoolYearAndSemester activeTerm = schoolYearAndSemesterRepository
                .findByStatus(Status.ACTIVE)
                .orElseThrow(() ->
                        new RuntimeException("No active school year and semester found.")
                );

        UserAccounts supervisor = userAccountsRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "User not found with id: " + userId
                        )
        );
        String programCode = resolveProgramCode(supervisor);
        String legacyDatabase = normalizeBlank(supervisor.getDataSource());

        if (legacyDatabase == null) {
            throw new AccessDeniedException("Supervisor data source is not configured.");
        }

        Pageable nativePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.unsorted()
        );

        Page<FacultyProgramLoadsDTO> result =
                primaryClassRepository.findFacultyPerProgram(
                        activeTerm.getSchoolYear(),
                        activeTerm.getSemester().getValue(),
                        legacyDatabase,
                        normalizeSearch(search),
                        normalizeBlank(campus),
                        programCode,
                        nativePageable
                );

        log.info(
                "Faculty program loads fetched successfully | totalElements={}",
                result.getTotalElements()
        );

        return result;
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    @Cacheable(
            value = "supervisorEvaluatedStudents",
            key = "#userId + ':' + (#searchTerm == null ? '' : #searchTerm.trim().toLowerCase()) + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()"
    )
    public Page<EvaluatedStudentsDTO> findEvaluatedStudents(
            Long userId,
            String searchTerm,
            Pageable pageable
    ) {

        log.info(
                "Fetching evaluated students | userId={} | searchTerm={} | page={} | size={}",
                userId,
                searchTerm,
                pageable.getPageNumber(),
                pageable.getPageSize()
        );

        Pageable nativeQueryPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.unsorted()
        );

        Page<EvaluatedStudentsDTO> result =
                facultyEvaluationScoreRepository.findEvaluatedStudents(
                                userId,
                                normalizeSearch(searchTerm),
                                nativeQueryPageable
                        )
                        .map(this::toEvaluatedStudentsDTO);

        log.info(
                "Evaluated students fetched successfully | userId={} | searchTerm={} | totalElements={}",
                userId,
                searchTerm,
                result.getTotalElements()
        );

        return result;
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public Page<ClassStudentEvaluationStatsResponse> findClassStudentEvaluationStats(
            Long userId,
            String search,
            String campus,
            Pageable pageable
    ) {
        SchoolYearAndSemester activeTerm = schoolYearAndSemesterRepository
                .findByStatus(Status.ACTIVE)
                .orElseThrow(() ->
                        new RuntimeException("No active school year and semester found.")
                );

        UserAccounts supervisor = userAccountsRepository.findById(userId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "User not found with id: " + userId
                        )
                );

        String programCode = resolveProgramCode(supervisor);
        String legacyDatabase = normalizeBlank(supervisor.getDataSource());

        if (legacyDatabase == null) {
            throw new AccessDeniedException("Supervisor data source is not configured.");
        }

        Pageable nativePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.unsorted()
        );

        Page<Object[]> classPage =
                primaryClassRepository.findClassStudentEvaluationStatClassPage(
                        activeTerm.getSchoolYear(),
                        activeTerm.getSemester().getValue(),
                        legacyDatabase,
                        normalizeSearch(search),
                        normalizeBlank(campus),
                        programCode,
                        nativePageable
                );

        if (classPage.isEmpty()) {
            return new PageImpl<>(
                    List.of(),
                    nativePageable,
                    classPage.getTotalElements()
            );
        }

        List<String> classCodes = classPage
                .getContent()
                .stream()
                .map(row -> safeString(row, 0))
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();

        Map<String, StudentEvaluationTotals> totalsByClassCode =
                findTotalsByClassCode(
                        classCodes,
                        activeTerm.getSchoolYear(),
                        activeTerm.getSemester().getValue()
                );

        List<ClassStudentEvaluationStatsResponse> content =
                classPage
                        .getContent()
                        .stream()
                        .map(row ->
                                toClassStudentEvaluationStatsResponse(
                                        row,
                                        totalsByClassCode.getOrDefault(
                                                safeString(row, 0),
                                                StudentEvaluationTotals.empty()
                                        )
                                )
                        )
                        .toList();

        return new PageImpl<>(
                content,
                nativePageable,
                classPage.getTotalElements()
        );
    }

    private EvaluatedStudentsDTO toEvaluatedStudentsDTO(Object[] row) {
        return new EvaluatedStudentsDTO(
                row[0] == null ? null : row[0].toString(),
                row[1] == null ? null : row[1].toString(),
                row[2] == null ? null : row[2].toString(),
                row[3] == null ? null : row[3].toString(),
                row[4] == null ? null : row[4].toString(),
                row[5] == null ? null : row[5].toString(),
                row[6] == null ? null : row[6].toString(),
                row[7] == null ? null : row[7].toString()
        );
    }

    private String normalizeSearch(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return null;
        }

        return searchTerm.trim();
    }

    private String normalizeBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private ClassStudentEvaluationStatsResponse toClassStudentEvaluationStatsResponse(
            Object[] row,
            StudentEvaluationTotals totals
    ) {
        Long totalStudents = totals.totalStudents();
        Long evaluatedStudents = totals.evaluatedStudents();
        Long pendingStudents = Math.max(totalStudents - evaluatedStudents, 0);
        Double evaluationPercentage = totalStudents == 0
                ? 0.0
                : Math.round((evaluatedStudents * 10000.0) / totalStudents) / 100.0;

        return new ClassStudentEvaluationStatsResponse(
                safeString(row, 0),
                safeString(row, 1),
                safeString(row, 2),
                safeString(row, 3),
                safeString(row, 4),
                safeString(row, 5),
                safeString(row, 6),
                totalStudents,
                evaluatedStudents,
                pendingStudents,
                evaluationPercentage
        );
    }

    private Map<String, StudentEvaluationTotals> findTotalsByClassCode(
            List<String> classCodes,
            Integer schoolYear,
            String semester
    ) {
        if (classCodes.isEmpty()) {
            return Map.of();
        }

        List<Object[]> totalRows =
                primaryClassRepository.findClassStudentEvaluationTotalsForClasses(
                        classCodes,
                        schoolYear,
                        semester
                );

        Map<String, StudentEvaluationTotals> totals = new HashMap<>();

        for (Object[] row : totalRows) {
            totals.put(
                    safeString(row, 0),
                    new StudentEvaluationTotals(
                            safeLong(row, 1),
                            safeLong(row, 2)
                    )
            );
        }

        return totals;
    }

    private record StudentEvaluationTotals(
            Long totalStudents,
            Long evaluatedStudents
    ) {
        private static StudentEvaluationTotals empty() {
            return new StudentEvaluationTotals(0L, 0L);
        }
    }

    private String safeString(Object[] row, int index) {
        Object value = row[index];
        return value == null ? "" : value.toString();
    }

    private Long safeLong(Object[] row, int index) {
        Object value = row[index];

        if (value == null) {
            return 0L;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(value.toString());
    }

    private String resolveProgramCode(UserAccounts userAccount) {
        if (userAccount == null || userAccount.getPrograms() == null) {
            return null;
        }

        String value = userAccount.getPrograms().getValue();

        if (value == null || value.isBlank() || value.equalsIgnoreCase("NULL")) {
            return null;
        }

        return value.trim();
    }

    private College parseCollege(String college) {
        try {
            return College.valueOf(college.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid college value: " + college);
        }
    }

    private Status parseStatus(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status value: " + status);
        }
    }

    private FacultyDTO mapToDTO(PrimaryFaculty f) {
        return new FacultyDTO(f.getFacultyId(), f.getLastname(), f.getFirstname(), f.getPosition(), f.getLoadLimit(), f.getMiddlename(), f.getCollege().name(), f.getStatus().name());
    }


}
