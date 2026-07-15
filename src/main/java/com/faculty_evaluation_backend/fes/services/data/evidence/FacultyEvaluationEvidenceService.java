package com.faculty_evaluation_backend.fes.services.data.evidence;

import com.faculty_evaluation_backend.fes.dto.evidence.EvaluationEvidenceCriterionResponse;
import com.faculty_evaluation_backend.fes.dto.evidence.FacultyEvaluationEvidenceResponse;
import com.faculty_evaluation_backend.fes.dto.evidence.FacultyEvaluationEvidenceSliceResponse;
import com.faculty_evaluation_backend.fes.entities.evaluation.FacultyEvaluationEvidence;
import com.faculty_evaluation_backend.fes.entities.evaluation.enums.EvaluationEvidenceCriterion;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationEvidenceRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.services.data.evidence.storage.EvidenceStorageService;
import com.faculty_evaluation_backend.fes.utilities.normalization.SemesterNormalizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FacultyEvaluationEvidenceService {

    private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;
    private static final int MAX_PAGE_SIZE = 50;
    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            ".pdf",
            ".doc",
            ".docx",
            ".xls",
            ".xlsx",
            ".jpg",
            ".jpeg",
            ".png"
    );
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png"
    );
    private static final Set<String> GENERIC_CONTENT_TYPES = Set.of(
            "application/octet-stream",
            "binary/octet-stream"
    );
    private static final Sort DEFAULT_SORT =
            Sort.by(Sort.Direction.DESC, "createdAt");
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "originalFilename",
            "criterion",
            "schoolYear",
            "semester",
            "classCode",
            "subjectCode",
            "uploadedBy",
            "fileSize"
    );

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

    @Cacheable(value = "evidenceCriteria", key = "'all'")
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
                        .semester(normalizeSemester(semester))
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

        Pageable optimizedPageable = optimizedPageable(pageable);

        return evidenceRepository.findByFilters(
                        facultyId.trim(),
                        blankToNull(classCode),
                        blankToNull(subjectCode),
                        normalizeSemester(semester),
                        schoolYear,
                        criterion,
                        optimizedPageable
                )
                .map(FacultyEvaluationEvidenceResponse::from);
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public FacultyEvaluationEvidenceSliceResponse listSlice(
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

        Slice<FacultyEvaluationEvidenceResponse> slice =
                evidenceRepository.findSliceByFilters(
                                facultyId.trim(),
                                blankToNull(classCode),
                                blankToNull(subjectCode),
                                normalizeSemester(semester),
                                schoolYear,
                                criterion,
                                optimizedPageable(pageable)
                        )
                        .map(FacultyEvaluationEvidenceResponse::from);

        return FacultyEvaluationEvidenceSliceResponse.from(slice);
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

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public String facultyIdForEvidence(Long evidenceId) {
        return evidenceRepository.findById(evidenceId)
                .map(FacultyEvaluationEvidence::getFacultyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Evidence not found.")
                );
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

        validateFileType(file);
    }

    private void validateFileType(MultipartFile file) {
        String originalFilename = cleanOriginalFilename(file.getOriginalFilename());
        String extension = extensionOf(originalFilename)
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
            throw new BadRequestException(
                    "Unsupported evidence file type. Allowed types: PDF, Word, Excel, JPG, and PNG."
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return;
        }

        String normalizedContentType = contentType
                .split(";", 2)[0]
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!ALLOWED_CONTENT_TYPES.contains(normalizedContentType)
                && !GENERIC_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new BadRequestException(
                    "Unsupported evidence content type: " + normalizedContentType
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

    private String normalizeSemester(String value) {
        return SemesterNormalizer.toCanonicalValue(value);
    }

    private Pageable optimizedPageable(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        Sort sort = safeSort(pageable.getSort());

        return PageRequest.of(page, size, sort);
    }

    private Sort safeSort(Sort requestedSort) {
        if (requestedSort == null || requestedSort.isUnsorted()) {
            return DEFAULT_SORT;
        }

        List<Sort.Order> orders = requestedSort.stream()
                .filter(order -> ALLOWED_SORT_FIELDS.contains(
                        order.getProperty()
                ))
                .map(order -> new Sort.Order(
                        order.getDirection(),
                        order.getProperty()
                ))
                .toList();

        if (orders.isEmpty()) {
            return DEFAULT_SORT;
        }

        return Sort.by(orders);
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
