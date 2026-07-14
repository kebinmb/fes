package com.faculty_evaluation_backend.fes.controller.supervisor;

import com.faculty_evaluation_backend.fes.audit.AuditableAction;
import com.faculty_evaluation_backend.fes.dto.evaluation.EvaluatedStudentsDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyLoadDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyProgramLoadsDTO;
import com.faculty_evaluation_backend.fes.dto.response.EvaluationCheckResponse;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.services.data.supervisor.SupervisorDataService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/faculty")
@Slf4j
@RequiredArgsConstructor
public class SupervisorDataController {
    private final SupervisorDataService supervisorDataService;

    @GetMapping("/list")
    @AuditableAction(action = "FETCH", entity = "FACULTY")
    public ResponseEntity<List<FacultyDTO>> getFacultiesByCollegeAndStatus(
            @RequestParam College college,
            @RequestParam Status status
    ) {
        return ResponseEntity.ok(
                supervisorDataService.getFacultiesByCollegeAndStatus(
                        college.name(),
                        status.name()
                )
        );
    }

    @GetMapping("/faculty-loads")
    @AuditableAction(action = "FETCH", entity = "FACULTY_LOADS")
    public ResponseEntity<Page<FacultyLoadDTO>> getFacultyLoadsByProgram(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "") String search,
            @PageableDefault(page = 0, size = 10, sort = "lastname")
            Pageable pageable,
            Authentication authentication
    ) {
        Long authenticatedUserId = authenticatedUserId(authentication);

        log.info(
                "API Request -> Faculty loads | userId={} | search={}",
                authenticatedUserId,
                search
        );

        return ResponseEntity.ok(
                supervisorDataService.getFacultyLoadsByProgram(
                        authenticatedUserId,
                        search,
                        pageable
                )
        );
    }

    @GetMapping("/faculty-classes")
    @AuditableAction(action = "FETCH", entity = "FACULTY_CLASSES")
    public ResponseEntity<List<FacultyClassDTO>> getFacultyClasses(
            @RequestParam @NotBlank String facultyId,
            Authentication authentication
    ) {
        Long authenticatedUserId = authenticatedUserId(authentication);

        log.info(
                "API Request -> Fetch faculty classes | userId={} | facultyId={}",
                authenticatedUserId,
                facultyId
        );

        return ResponseEntity.ok(
                supervisorDataService.findFacultyClassesForSupervisor(
                        authenticatedUserId,
                        facultyId
                )
        );
    }

    @GetMapping("/faculty-program-loads")
    @AuditableAction(action = "FETCH", entity = "FACULTY_PROGRAM_LOADS")
    public ResponseEntity<Page<FacultyProgramLoadsDTO>> getFacultyProgramLoads(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false, defaultValue = "") String campus,
            @PageableDefault(page = 0, size = 10, sort = "lastname")
            Pageable pageable,
            Authentication authentication
    ) {
        Long authenticatedUserId = authenticatedUserId(authentication);

        log.info(
                "API Request -> Fetch faculty program loads | userId={} | search={} | campus={}",
                authenticatedUserId,
                search,
                campus
        );

        return ResponseEntity.ok(
                supervisorDataService.getFacultyInPrograms(
                        authenticatedUserId,
                        search,
                        campus,
                        pageable
                )
        );
    }

    @GetMapping("/check")
    @AuditableAction(action = "CHECK", entity = "FACULTY_EVALUATION_STATUS")
    public ResponseEntity<?> checkEvaluationStatus(
            @RequestParam String facultyId,
            @RequestParam(required = false) String evaluatorId,
            @RequestParam String classCode,
            @RequestParam String subjectCode,
            @RequestParam String yearLevel,
            @RequestParam String semester,
            @RequestParam Integer schoolYear,
            Authentication authentication
    ) {
        try {
            String authenticatedEvaluatorId = authenticatedUserId(authentication)
                    .toString();
            boolean hasEvaluated = supervisorDataService.hasEvaluated(
                    facultyId,
                    authenticatedEvaluatorId,
                    classCode,
                    subjectCode,
                    yearLevel,
                    semester,
                    schoolYear
            );

            EvaluationCheckResponse response =
                    EvaluationCheckResponse.builder()
                            .hasEvaluated(hasEvaluated)
                            .message(hasEvaluated
                                    ? "You have already evaluated this faculty for this subject"
                                    : "You can submit an evaluation")
                            .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking evaluation status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GetMapping("/evaluated-students")
    @AuditableAction(action = "FETCH", entity = "EVALUATED_STUDENTS")
    public ResponseEntity<Page<EvaluatedStudentsDTO>> getEvaluatedStudents(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false, defaultValue = "") String searchTerm,
            @PageableDefault(page = 0, size = 10, sort = "createdAt")
            Pageable pageable,
            Authentication authentication
    ) {
        Long authenticatedUserId = authenticatedUserId(authentication);

        return ResponseEntity.ok(
                supervisorDataService.findEvaluatedStudents(
                        authenticatedUserId,
                        searchTerm,
                        pageable
                )
        );
    }

    private Long authenticatedUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalStateException("Authenticated user is required.");
        }

        return Long.parseLong(authentication.getName());
    }
}
