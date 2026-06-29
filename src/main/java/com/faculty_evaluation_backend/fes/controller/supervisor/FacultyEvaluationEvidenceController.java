package com.faculty_evaluation_backend.fes.controller.supervisor;

import com.faculty_evaluation_backend.fes.dto.evidence.EvaluationEvidenceCriterionResponse;
import com.faculty_evaluation_backend.fes.dto.evidence.FacultyEvaluationEvidenceResponse;
import com.faculty_evaluation_backend.fes.dto.evidence.FacultyEvaluationEvidenceSliceResponse;
import com.faculty_evaluation_backend.fes.services.data.evidence.FacultyEvaluationEvidenceService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/faculty/evidences")
@RequiredArgsConstructor
public class FacultyEvaluationEvidenceController {

    private final FacultyEvaluationEvidenceService evidenceService;

    @GetMapping("/criteria")
    public ResponseEntity<List<EvaluationEvidenceCriterionResponse>>
    listCriteria() {
        return ResponseEntity.ok(evidenceService.listCriteria());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FacultyEvaluationEvidenceResponse> upload(
            @RequestParam @NotBlank String facultyId,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) String yearLevel,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer schoolYear,
            @RequestParam @NotBlank String criterion,
            @RequestParam(required = false) String description,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                evidenceService.upload(
                        facultyId,
                        classCode,
                        subjectCode,
                        yearLevel,
                        semester,
                        schoolYear,
                        criterion,
                        description,
                        currentUser(authentication),
                        file
                )
        );
    }

    @GetMapping
    public ResponseEntity<Page<FacultyEvaluationEvidenceResponse>> list(
            @RequestParam @NotBlank String facultyId,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer schoolYear,
            @RequestParam(required = false) String criterion,
            @PageableDefault(
                    page = 0,
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                evidenceService.list(
                        facultyId,
                        classCode,
                        subjectCode,
                        semester,
                        schoolYear,
                        criterion,
                        pageable
                )
        );
    }

    @GetMapping("/slice")
    public ResponseEntity<FacultyEvaluationEvidenceSliceResponse> listSlice(
            @RequestParam @NotBlank String facultyId,
            @RequestParam(required = false) String classCode,
            @RequestParam(required = false) String subjectCode,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer schoolYear,
            @RequestParam(required = false) String criterion,
            @PageableDefault(
                    page = 0,
                    size = 12,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                evidenceService.listSlice(
                        facultyId,
                        classCode,
                        subjectCode,
                        semester,
                        schoolYear,
                        criterion,
                        pageable
                )
        );
    }

    @GetMapping("/{evidenceId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable @NotNull Long evidenceId
    ) {
        FacultyEvaluationEvidenceService.EvidenceDownload download =
                evidenceService.download(evidenceId);

        MediaType mediaType = download.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(download.contentType());

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.filename())
                                .build()
                                .toString()
                )
                .body(download.resource());
    }

    @DeleteMapping("/{evidenceId}")
    public ResponseEntity<Void> delete(
            @PathVariable @NotNull Long evidenceId
    ) {
        evidenceService.delete(evidenceId);
        return ResponseEntity.noContent().build();
    }

    private String currentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return "UNKNOWN";
        }

        return authentication.getName();
    }
}
