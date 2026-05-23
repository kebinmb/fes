package com.faculty_evaluation_backend.fes.controller.administrator;

import com.faculty_evaluation_backend.fes.audit.AuditableAction;
import com.faculty_evaluation_backend.fes.controller.evaluation.EvaluationController;
import com.faculty_evaluation_backend.fes.dto.data.SchoolYearAndSemesterDTO;
import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationPrintResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyEvaluationScoreResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchUserAccountsResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.entities.data.enums.Semester;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.services.data.admin.AdministratorService;
import com.faculty_evaluation_backend.fes.services.data.evaluation.EvaluationDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdministratorController {
    private final AdministratorService administratorService;
    private final EvaluationDataService evaluationDataService;

    @GetMapping("/faculties")
    public PageResponse<FetchFacultyResponse> getFacultyList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search) {

        log.info("Fetching faculty list | page: {} size: {} search: {}", page, size, search);

        return administratorService.facultyList(page, size, search);
    }

    @GetMapping("/user-accounts")
    public PageResponse<FetchUserAccountsResponse> getAccounts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return administratorService.accountList(page, size);
    }

    @GetMapping("/faculty-evaluation-score")
    public PageResponse<FetchFacultyEvaluationScoreResponse> getFacultyEvaluationScores(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching faculty evaluation scores | page: {} size: {}", page, size);

        return administratorService.facultyEvaluationScore(page, size);
    }

    @PutMapping("/update-faculty")
    public ResponseEntity<String> updateFaculty(@RequestParam String facultyId, @RequestParam String firstname, @RequestParam(required = false) String middlename, @RequestParam String lastname, @RequestParam String position, @RequestParam Double loadLimit, @RequestParam College college, @RequestParam Status status) {

        log.info("Updating faculty with facultyId: {}", facultyId);

        String response = administratorService.updateFaculty(facultyId, firstname, middlename, lastname, position, loadLimit, college, status);

        return ResponseEntity.ok(response);
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

    @PutMapping("/school-year-semester")
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

}
