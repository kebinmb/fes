package com.faculty_evaluation_backend.fes.controller.administrator;

import com.faculty_evaluation_backend.fes.audit.AuditableAction;
import com.faculty_evaluation_backend.fes.audit.AuditLogService;
import com.faculty_evaluation_backend.fes.dto.audit.AuditLogSliceResponse;
import com.faculty_evaluation_backend.fes.controller.evaluation.EvaluationController;
import com.faculty_evaluation_backend.fes.dto.audit.AuditLogResponse;
import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationGeneratedReportResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardFacultyLoadResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardProgramBreakdownResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.AdminDashboardSummaryResponse;
import com.faculty_evaluation_backend.fes.dto.dashboard.FacultyWorkloadCoverageResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationPrintResponse;
import com.faculty_evaluation_backend.fes.dto.evaluation.StudentEvaluationStatusResponse;
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
import com.faculty_evaluation_backend.fes.entities.authentication.CustomUserDetails;
import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.services.data.admin.AdministratorService;
import com.faculty_evaluation_backend.fes.services.data.evaluation.EvaluationDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdministratorController {
    private final AdministratorService administratorService;
    private final EvaluationDataService evaluationDataService;
    private final AuditLogService auditLogService;

    @GetMapping("/dashboard")
    @AuditableAction(action = "FETCH", entity = "ADMIN_DASHBOARD")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(
                administratorService.getDashboard()
        );
    }

    @GetMapping("/dashboard/summary")
    @AuditableAction(action = "FETCH", entity = "ADMIN_DASHBOARD_SUMMARY")
    public ResponseEntity<AdminDashboardSummaryResponse> getDashboardSummary() {
        return ResponseEntity.ok(
                administratorService.getDashboardSummary()
        );
    }

    @GetMapping("/dashboard/programs")
    @AuditableAction(action = "FETCH", entity = "ADMIN_DASHBOARD_PROGRAMS")
    public ResponseEntity<List<AdminDashboardProgramBreakdownResponse>>
    getDashboardProgramBreakdown() {
        return ResponseEntity.ok(
                administratorService.getDashboardProgramBreakdown()
        );
    }

    @GetMapping("/dashboard/faculty-loads")
    @AuditableAction(action = "FETCH", entity = "ADMIN_DASHBOARD_FACULTY_LOADS")
    public ResponseEntity<List<AdminDashboardFacultyLoadResponse>>
    getDashboardFacultyLoads(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                administratorService.getDashboardFacultyLoads(
                        limit
                )
        );
    }

    @GetMapping("/dashboard/faculty-workload-coverage")
    @AuditableAction(action = "FETCH", entity = "FACULTY_WORKLOAD_COVERAGE")
    public ResponseEntity<FacultyWorkloadCoverageResponse>
    getFacultyWorkloadCoverage() {
        return ResponseEntity.ok(
                administratorService.getFacultyWorkloadCoverage()
        );
    }

    @GetMapping("/faculties")
    @AuditableAction(action = "FETCH", entity = "FACULTY")
    public PageResponse<FetchFacultyResponse> getFacultyList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String legacyDatabase
    ) {

        log.info(
                "Fetching faculty list | page: {} size: {} search: {} legacyDatabase: {}",
                page,
                size,
                search,
                legacyDatabase
        );

        return administratorService.facultyList(page, size, search, legacyDatabase);
    }

    @GetMapping("/class-assignments")
    @AuditableAction(action = "FETCH", entity = "PRIMARY_CLASS_ASSIGNMENT")
    public PageResponse<ClassFacultyAssignmentResponse> getClassAssignments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String legacyDatabase
    ) {
        return administratorService.getClassAssignments(
                page,
                size,
                search,
                legacyDatabase
        );
    }

    @GetMapping("/class-assignments/faculties")
    @AuditableAction(action = "FETCH", entity = "CLASS_ASSIGNMENT_FACULTY_OPTIONS")
    public ResponseEntity<List<FacultyAssignmentOptionResponse>>
    getClassAssignmentFacultyOptions(
            @RequestParam(required = false) String legacyDatabase
    ) {
        return ResponseEntity.ok(
                administratorService.getClassAssignmentFacultyOptions(
                        legacyDatabase
                )
        );
    }

    @PatchMapping("/class-assignments/{primaryClassId}/faculty")
    @AuditableAction(action = "REASSIGN", entity = "PRIMARY_CLASS_FACULTY")
    public ResponseEntity<ClassFacultyReassignmentResponse> reassignClassFaculty(
            @PathVariable Long primaryClassId,
            @Valid @RequestBody ClassFacultyReassignmentRequest request
    ) {
        return ResponseEntity.ok(
                administratorService.reassignClassFaculty(
                        primaryClassId,
                        request
                )
        );
    }

    @GetMapping("/user-accounts")
    @AuditableAction(action = "FETCH", entity = "USER_ACCOUNTS")
    public PageResponse<FetchUserAccountsResponse> getAccounts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return administratorService.accountList(page, size);
    }

    @GetMapping("/faculty-evaluation-score")
    @AuditableAction(action = "FETCH", entity = "FACULTY_EVALUATION_SCORE")
    public PageResponse<FetchFacultyEvaluationScoreResponse> getFacultyEvaluationScores(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching faculty evaluation scores | page: {} size: {}", page, size);

        return administratorService.facultyEvaluationScore(page, size);
    }

    @PutMapping("/update-faculty")
    @AuditableAction(action = "UPDATE", entity = "FACULTY")
    public ResponseEntity<String> updateFaculty(@RequestParam String facultyId, @RequestParam String firstname, @RequestParam(required = false) String middlename, @RequestParam String lastname, @RequestParam String position, @RequestParam Double loadLimit, @RequestParam College college, @RequestParam Status status) {

        log.info("Updating faculty with facultyId: {}", facultyId);

        String response = administratorService.updateFaculty(facultyId, firstname, middlename, lastname, position, loadLimit, college, status);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/faculty-workloads")
    @AuditableAction(action = "FETCH", entity = "FACULTY_WORKLOAD")
    public PageResponse<FacultyWorkloadResponse> getFacultyWorkloads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer schoolYear,
            @RequestParam(required = false) String semester
    ) {
        return administratorService.getFacultyWorkloads(
                page,
                size,
                search,
                schoolYear,
                semester
        );
    }

    @PutMapping("/faculty-workloads")
    @AuditableAction(action = "UPSERT", entity = "FACULTY_WORKLOAD")
    public ResponseEntity<FacultyWorkloadResponse> upsertFacultyWorkload(
            @Valid @RequestBody FacultyWorkloadRequest request
    ) {
        return ResponseEntity.ok(
                administratorService.upsertFacultyWorkload(request)
        );
    }

    @GetMapping("/faculty-workloads/record")
    @AuditableAction(action = "FETCH", entity = "FACULTY_WORKLOAD_RECORD")
    public ResponseEntity<FacultyWorkloadResponse> getFacultyWorkload(
            @RequestParam String facultyId,
            @RequestParam Integer schoolYear,
            @RequestParam String semester,
            @RequestParam(required = false) String classCode,
            @RequestParam String courseCode,
            @RequestParam String programCode,
            @RequestParam String yearLevel,
            @RequestParam String sectionCode
    ) {
        return ResponseEntity.ok(
                administratorService.getFacultyWorkload(
                        facultyId,
                        schoolYear,
                        semester,
                        classCode,
                        courseCode,
                        programCode,
                        yearLevel,
                        sectionCode
                )
        );
    }

    @GetMapping("/faculty-workloads/{facultyWorkloadId}")
    @AuditableAction(action = "FETCH", entity = "FACULTY_WORKLOAD")
    public ResponseEntity<FacultyWorkloadResponse> getFacultyWorkloadById(
            @PathVariable Long facultyWorkloadId
    ) {
        return ResponseEntity.ok(
                administratorService.getFacultyWorkloadById(
                        facultyWorkloadId
                )
        );
    }

    @GetMapping("/faculty-workloads/section-options")
    @AuditableAction(action = "FETCH", entity = "FACULTY_WORKLOAD_SECTION_OPTIONS")
    public ResponseEntity<List<FacultyWorkloadSectionOptionResponse>>
    getFacultyWorkloadSectionOptions(
            @RequestParam Integer schoolYear,
            @RequestParam String semester
    ) {
        return ResponseEntity.ok(
                administratorService.getFacultyWorkloadSectionOptions(
                        schoolYear,
                        semester
                )
        );
    }

    @GetMapping("/faculty-workloads/class-options")
    @AuditableAction(action = "FETCH", entity = "FACULTY_WORKLOAD_CLASS_OPTIONS")
    public ResponseEntity<List<FacultyWorkloadClassOptionResponse>>
    getFacultyWorkloadClassOptions(
            @RequestParam String facultyId,
            @RequestParam Integer schoolYear,
            @RequestParam String semester
    ) {
        return ResponseEntity.ok(
                administratorService.getFacultyWorkloadClassOptions(
                        facultyId,
                        schoolYear,
                        semester
                )
        );
    }

    @GetMapping("/faculty-evaluation-score/{facultyId}")
    @AuditableAction(action = "FETCH", entity = "FACULTY_EVALUATION_REPORT")
    public ResponseEntity<?> getFacultyEvaluationScoresByFacultyId(
            @PathVariable String facultyId
    ) {

        log.info("📄 Fetching evaluation scores for facultyId: {}", facultyId);

        try {

            List<FacultyEvaluationPrintResponse> evaluations =
                    evaluationDataService
                            .getSumOfAllFacultyEvaluationPerSubject(facultyId);

            return ResponseEntity.ok(evaluations);

        } catch (RuntimeException ex) {

            log.error("❌ Failed fetching evaluations: {}", ex.getMessage());

            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("FAILED", ex.getMessage()));
        }
    }

    record ErrorResponse(String status, String message) {
    }

    @PostMapping("/faculty-evaluation-reports/{facultyId}")
    @AuditableAction(action = "GENERATE", entity = "FACULTY_EVALUATION_REPORT")
    public ResponseEntity<FacultyEvaluationGeneratedReportResponse>
    generateFacultyEvaluationReport(
            @PathVariable String facultyId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletRequest request
    ) {
        String verificationBaseUrl =
                request.getScheme()
                        + "://"
                        + request.getServerName()
                        + (request.getServerPort() == 80
                        || request.getServerPort() == 443
                        ? ""
                        : ":" + request.getServerPort());

        return ResponseEntity.ok(
                evaluationDataService.generateFacultyEvaluationReport(
                        facultyId,
                        verificationBaseUrl,
                        userDetails
                )
        );
    }

    @PutMapping("/school-year-semester")
    @AuditableAction(action = "UPDATE", entity = "SCHOOL_YEAR_SEMESTER")
    public ResponseEntity<?> updateSchoolYearAndSemester(
            @RequestParam Integer schoolYear,
            @RequestParam Semester semester
    ) {

        try {

            log.info(
                    "Updating school year and semester | schoolYear={} | semester={}",
                    schoolYear,
                    semester
            );

            SchoolYearAndSemesterDTO response =
                    administratorService
                            .updateSchoolYearAndSemester(
                                    schoolYear,
                                    semester
                            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            log.error(
                    "Failed to update school year and semester | error={}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());

        } catch (Exception e) {

            log.error(
                    "Unexpected error updating school year and semester",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Failed to update school year and semester."
                    );
        }
    }

    @GetMapping("/school-year-semester")
    @AuditableAction(action = "FETCH", entity = "SCHOOL_YEAR_SEMESTER")
    public ResponseEntity<?> fetchCurrentSchoolYearAndSemester() {

        try {

            log.info(
                    "Fetching current active school year and semester"
            );

            SchoolYearAndSemesterDTO response =
                    administratorService
                            .fetchCurrentSchoolYearAndSemester();

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            log.error(
                    "Failed to fetch current school year and semester | error={}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (Exception e) {

            log.error(
                    "Unexpected error fetching school year and semester",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Failed to fetch school year and semester."
                    );
        }
    }
    @GetMapping("/student-faculty-evaluation")
    @AuditableAction(action = "FETCH", entity = "STUDENT_FACULTY_EVALUATION")
    public ResponseEntity<?> getStudentFacultyEvaluationDetails(

            @RequestParam(defaultValue = "") String search,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "created_at") String sortBy,

            @RequestParam(defaultValue = "desc") String sortDirection
    ) {

        try {

            log.info(
                    "Fetching student faculty evaluation details | search={} | page={} | size={}",
                    search,
                    page,
                    size
            );

            return ResponseEntity.ok(
                    administratorService.getStudentFacultyEvaluationDetails(
                            search,
                            page,
                            size,
                            sortBy,
                            sortDirection
                    )
            );

        } catch (RuntimeException e) {

            log.error(
                    "Failed to fetch student faculty evaluation details | error={}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ErrorResponse(
                                    "FAILED",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            log.error(
                    "Unexpected error fetching student faculty evaluation details",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ErrorResponse(
                                    "FAILED",
                                    "Failed to fetch student faculty evaluation details."
                            )
                    );
        }
    }
    @PostMapping("/create-user")
    @AuditableAction(action = "CREATE", entity = "USER_ACCOUNTS")
    public ResponseEntity<String> createUser(
            @RequestBody CreateUserAccountDTO dto
    ) {

        return ResponseEntity.ok(
                administratorService.createUserAccount(dto)
        );
    }

    @PutMapping("/update-user")
    @AuditableAction(action = "UPDATE", entity = "USER_ACCOUNTS")
    public ResponseEntity<String> updateUser(
            @RequestBody UpdateUserAccountDTO dto
    ) {

        return ResponseEntity.ok(
                administratorService.updateUserAccount(dto)
        );
    }

    @PutMapping("/update-password")
    @AuditableAction(action = "UPDATE_PASSWORD", entity = "USER_ACCOUNTS")
    public ResponseEntity<String> updatePassword(
            @RequestBody UpdateUserPasswordDTO dto
    ) {

        return ResponseEntity.ok(
                administratorService.updateUserPassword(dto)
        );
    }

    @GetMapping("/student-sections")
    @AuditableAction(action = "FETCH", entity = "STUDENT_SECTIONS")
    public ResponseEntity<?> getStudentSections(

            @RequestParam(required = false) String programCode,

            @RequestParam(required = false) String yearLevel,

            @RequestParam(required = false) String sectionCode,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size
    ) {

        try {

            log.info(
                    """
                    Fetching student section evaluations |
                    programCode={} |
                    yearLevel={} |
                    sectionCode={} |
                    page={} |
                    size={}
                    """,
                    programCode,
                    yearLevel,
                    sectionCode,
                    page,
                    size
            );

            PageResponse<StudentSectionDTO> response =
                    administratorService.getStudentSections(
                            programCode,
                            yearLevel,
                            sectionCode,
                            page,
                            size
                    );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            log.error(
                    "Failed to fetch student section evaluations | error={}",
                    e.getMessage(),
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            new ErrorResponse(
                                    "FAILED",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            log.error(
                    "Unexpected error fetching student section evaluations",
                    e
            );

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            new ErrorResponse(
                                    "FAILED",
                                    "Failed to fetch student section evaluations."
                            )
                    );
        }
    }

    @GetMapping("/student-evaluation-status")
    @AuditableAction(action = "FETCH", entity = "STUDENT_EVALUATION_STATUS")
    public ResponseEntity<List<StudentEvaluationStatusResponse>>
    fetchStudentEvaluationStatus(

            @RequestParam String programCode,

            @RequestParam String yearLevel,

            @RequestParam String sectionCode
    ) {

        return ResponseEntity.ok(

                administratorService
                        .fetchStudentEvaluationStatus(

                                programCode,

                                yearLevel,

                                sectionCode
                        )
        );
    }

    @GetMapping("/audit-logs")
    @AuditableAction(action = "FETCH", entity = "AUDIT_LOG")
    public PageResponse<AuditLogResponse> getAuditLogs(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return auditLogService.findAuditLogs(
                userId,
                username,
                action,
                entityType,
                startDate,
                endDate,
                search,
                page,
                size
        );
    }

    @GetMapping("/audit-logs/slice")
    @AuditableAction(action = "FETCH", entity = "AUDIT_LOG")
    public AuditLogSliceResponse getAuditLogSlice(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int size
    ) {
        return auditLogService.findAuditLogSlice(
                userId,
                username,
                action,
                entityType,
                startDate,
                endDate,
                search,
                page,
                size
        );
    }
}
