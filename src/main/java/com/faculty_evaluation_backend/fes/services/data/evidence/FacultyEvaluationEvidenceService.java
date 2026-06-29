package com.faculty_evaluation_backend.fes.services.data.evidence;

import com.faculty_evaluation_backend.fes.dto.evidence.EvaluationEvidenceCriterionResponse;
import com.faculty_evaluation_backend.fes.dto.evidence.FacultyEvaluationEvidenceResponse;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationEvidence;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.EvaluationEvidenceCriterion;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationEvidenceRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.services.data.evidence.storage.EvidenceStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
public class FacultyEvaluationEvidenceService {

    private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;

    private final FacultyEvaluationEvidenceRepository evidenceRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;
    private final EvidenceStorageService storageService;
    private final long maxFileSizeBytes;

    public FacultyEvaluationEvidenceService(
            FacultyEvaluationEvidenceRepository evidenceRepository,
            PrimaryFacultyRepository primaryFacultyRepository,
            EvidenceStorageService storageService,
            @Value("${app.evidence.max-file-size:15MB}") DataSize maxFileSize
    ) {
        this.evidenceRepository = evidenceRepository;
        this.primaryFacultyRepository = primaryFacultyRepository;
        this.storageService = storageService;
        this.maxFileSizeBytes = maxFileSize.toBytes();
    }

    public List<EvaluationEvidenceCriterionResponse> listCriteria() {
        return EvaluationEvidenceCriterion.list()
                .stream()
                .map(EvaluationEvidenceCriterionResponse::from)
                .toList();
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyEvaluationEvidenceResponse upload(
            String facultyId,
            String classCode,
            String subjectCode,
            String yearLevel,
            String semester,
            Integer schoolYear,
            String criterionValue,
            String description,
            String uploadedBy,
            MultipartFile file
    ) {
        validateUpload(facultyId, uploadedBy, file);

        primaryFacultyRepository.findByFacultyId(facultyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Faculty not found.")
                );

        EvaluationEvidenceCriterion criterion =
                parseCriterion(criterionValue);

        String originalFilename =
                cleanOriginalFilename(file.getOriginalFilename());

        String storedFilename =
                UUID.randomUUID() + extensionOf(originalFilename);

        EvidenceStorageService.StoredEvidenceFile storedFile =
                storageService.store(
                        file,
                        originalFilename,
                        storedFilename,
                        facultyId.trim()
                );

        FacultyEvaluationEvidence evidence =
                FacultyEvaluationEvidence.builder()
                        .facultyId(facultyId.trim())
                        .classCode(blankToNull(classCode))
                        .subjectCode(blankToNull(subjectCode))
                        .yearLevel(blankToNull(yearLevel))
                        .semester(blankToNull(semester))
                        .schoolYear(schoolYear)
                        .criterion(criterion)
                        .description(blankToNull(description))
                        .uploadedBy(uploadedBy.trim())
                        .originalFilename(originalFilename)
                        .storedFilename(storedFile.storedFilename())
                        .contentType(storedFile.contentType())
                        .fileSize(storedFile.fileSize())
                        .filePath(storedFile.filePath())
                        .build();

        return FacultyEvaluationEvidenceResponse.from(
                evidenceRepository.save(evidence)
        );
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public Page<FacultyEvaluationEvidenceResponse> list(
            String facultyId,
            String classCode,
            String subjectCode,
            String semester,
            Integer schoolYear,
            String criterionValue,
            Pageable pageable
    ) {
        if (facultyId == null || facultyId.isBlank()) {
            throw new BadRequestException("Faculty ID is required.");
        }

        EvaluationEvidenceCriterion criterion =
                criterionValue == null || criterionValue.isBlank()
                        ? null
                        : parseCriterion(criterionValue);

        return evidenceRepository.findByFilters(
                        facultyId.trim(),
                        blankToNull(classCode),
                        blankToNull(subjectCode),
                        blankToNull(semester),
                        schoolYear,
                        criterion,
                        pageable
                )
                .map(FacultyEvaluationEvidenceResponse::from);
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public EvidenceDownload download(Long evidenceId) {
        FacultyEvaluationEvidence evidence =
                evidenceRepository.findById(evidenceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Evidence not found."
                                )
                        );

        EvidenceStorageService.EvidenceDownload download =
                storageService.download(
                        evidence.getFilePath(),
                        evidence.getOriginalFilename(),
                        evidence.getContentType()
                );

        return new EvidenceDownload(
                download.resource(),
                download.filename(),
                download.contentType()
        );
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public void delete(Long evidenceId) {
        FacultyEvaluationEvidence evidence =
                evidenceRepository.findById(evidenceId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Evidence not found."
                                )
                        );

        evidenceRepository.delete(evidence);
        storageService.delete(evidence.getFilePath());
    }

    private void validateUpload(
            String facultyId,
            String uploadedBy,
            MultipartFile file
    ) {
        if (facultyId == null || facultyId.isBlank()) {
            throw new BadRequestException("Faculty ID is required.");
        }

        if (uploadedBy == null || uploadedBy.isBlank()) {
            throw new BadRequestException("Uploader is required.");
        }

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Evidence file is required.");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BadRequestException(
                    "Evidence file must not exceed "
                            + readableFileSize(maxFileSizeBytes)
                            + "."
            );
        }
    }

    private EvaluationEvidenceCriterion parseCriterion(String criterionValue) {
        try {
            return EvaluationEvidenceCriterion.from(criterionValue);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    private String cleanOriginalFilename(String originalFilename) {
        String filename = StringUtils.cleanPath(
                originalFilename == null ? "evidence" : originalFilename
        );

        if (filename.contains("..")) {
            throw new BadRequestException("Invalid evidence filename.");
        }

        return filename;
    }

    private String extensionOf(String filename) {
        int index = filename.lastIndexOf('.');

        if (index < 0 || index == filename.length() - 1) {
            return "";
        }

        return filename.substring(index);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private String readableFileSize(long bytes) {
        if (bytes % BYTES_PER_MEGABYTE == 0) {
            return (bytes / BYTES_PER_MEGABYTE) + "MB";
        }

        return bytes + " bytes";
    }

    public record EvidenceDownload(
            Resource resource,
            String filename,
            String contentType
    ) {
    }
}
