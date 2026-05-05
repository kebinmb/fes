package com.faculty_evaluation_backend.fes.controller.administrator;

import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyEvaluationScoreResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchFacultyResponse;
import com.faculty_evaluation_backend.fes.dto.response.FetchUserAccountsResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import com.faculty_evaluation_backend.fes.services.data.admin.AdministratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdministratorController {
    private final AdministratorService administratorService;

    @GetMapping("/faculties")
    public PageResponse<FetchFacultyResponse> getFacultyList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        log.info("Fetching faculty list | page: {} size: {}", page, size);

        return administratorService.facultyList(page, size);
    }

    @GetMapping("/user-accounts")
    public PageResponse<FetchUserAccountsResponse> getAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return administratorService.accountList(page, size);
    }

    @GetMapping("/faculty-evaluation-score")
    public PageResponse<FetchFacultyEvaluationScoreResponse> getFacultyEvaluationScores(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        log.info("Fetching faculty evaluation scores | page: {} size: {}", page, size);

        return administratorService.facultyEvaluationScore(page, size);
    }
}
