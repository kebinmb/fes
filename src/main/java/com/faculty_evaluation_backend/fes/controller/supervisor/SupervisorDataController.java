package com.faculty_evaluation_backend.fes.controller.supervisor;

import com.faculty_evaluation_backend.fes.dto.faculty.FacultyClassDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyDTO;
import com.faculty_evaluation_backend.fes.dto.faculty.FacultyLoadDTO;
import com.faculty_evaluation_backend.fes.dto.response.EvaluationCheckResponse;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Programs;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.services.data.supervisor.SupervisorDataService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/faculty")
@Slf4j
@RequiredArgsConstructor
public class SupervisorDataController {

    private final SupervisorDataService supervisorDataService;


    //TODO : Change this and use the Program instead of the College
    @GetMapping("/list")
    public ResponseEntity<List<FacultyDTO>> getFacultiesByCollegeAndStatus(@RequestParam College college, @RequestParam Status status) {
        return ResponseEntity.ok(supervisorDataService.getFacultiesByCollegeAndStatus(college.name(), status.name()));
    }

    @GetMapping("/faculty-loads")
    public ResponseEntity<Page<FacultyLoadDTO>> getFacultyLoadsByProgram(

            @RequestParam Long userId,

            @RequestParam(required = false, defaultValue = "") String search,

            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "lastname"
            ) Pageable pageable
    ) {

        log.info("Search received: [{}]", search);

        log.info(
                "API Request → Faculty loads | userId={} | search={}",
                userId,
                search
        );

        Page<FacultyLoadDTO> response =
                supervisorDataService.getFacultyLoadsByProgram(
                        userId,
                        search,
                        pageable
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/faculty-classes")
    public ResponseEntity<List<FacultyClassDTO>> getFacultyClasses(

            @RequestParam
            @NotBlank
            String facultyId
    ) {

        log.info(
                "API Request → Fetch faculty classes | facultyId={}",
                facultyId
        );

        return ResponseEntity.ok(
                supervisorDataService.findFacultyClasses(
                        facultyId
                )
        );
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkEvaluationStatus(@RequestParam String facultyId, @RequestParam String evaluatorId, @RequestParam String classCode, @RequestParam String subjectCode, @RequestParam String yearLevel, @RequestParam String semester, @RequestParam Integer schoolYear) {
        try {
            boolean hasEvaluated = supervisorDataService.hasEvaluated(facultyId, evaluatorId, classCode, subjectCode, yearLevel, semester, schoolYear);

            EvaluationCheckResponse response = EvaluationCheckResponse.builder().hasEvaluated(hasEvaluated).message(hasEvaluated ? "You have already evaluated this faculty for this subject" : "You can submit an evaluation").build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Error checking evaluation status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}