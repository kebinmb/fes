package com.faculty_evaluation_backend.fes.controller.administrator;

import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyEvaluationScoreResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchUserAccountsResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.entities.primary.enums.College;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.services.data.admin.AdministratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdministratorController {
    private final AdministratorService administratorService;

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
}
