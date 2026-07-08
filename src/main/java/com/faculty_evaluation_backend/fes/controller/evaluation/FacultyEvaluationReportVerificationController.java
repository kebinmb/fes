package com.faculty_evaluation_backend.fes.controller.evaluation;

import com.faculty_evaluation_backend.fes.dto.evaluation.FacultyEvaluationReportVerificationResponse;
import com.faculty_evaluation_backend.fes.services.data.evaluation.EvaluationDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FacultyEvaluationReportVerificationController {

    private final EvaluationDataService evaluationDataService;

    @GetMapping("/verify-report/{reportId}")
    public ResponseEntity<FacultyEvaluationReportVerificationResponse>
    verifyReport(@PathVariable String reportId) {
        return ResponseEntity.ok(
                evaluationDataService.verifyReport(reportId)
        );
    }
}
