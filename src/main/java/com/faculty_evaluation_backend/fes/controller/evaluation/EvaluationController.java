package com.faculty_evaluation_backend.fes.controller.evaluation;

import com.faculty_evaluation_backend.fes.audit.AuditableAction;
import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationScore;
import com.faculty_evaluation_backend.fes.services.data.evaluation.EvaluationDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/evaluation")
@RequiredArgsConstructor
@Slf4j
public class EvaluationController {
    private final EvaluationDataService evaluationDataService;

    @PostMapping("/submit")
    @AuditableAction(action="SUBMIT",entity = "EVALUATION")
    public ResponseEntity<?> submitEvaluation(@RequestBody BaseEvaluationDTO dto) {

        log.info("📥 API Request - Submit Evaluation: {}", dto.getEvaluationType());

        try {
            FacultyEvaluationScore saved = evaluationDataService.submit(dto);

            return ResponseEntity.ok(saved);

        } catch (RuntimeException ex) {

            log.error("❌ Submission failed: {}", ex.getMessage());

            return ResponseEntity.badRequest().body(
                    new ErrorResponse("FAILED", ex.getMessage())
            );
        }
    }

    // 🔥 Simple error wrapper (clean API response)
    record ErrorResponse(String status, String message) {}
}
