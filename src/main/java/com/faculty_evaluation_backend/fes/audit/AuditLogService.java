package com.faculty_evaluation_backend.fes.audit;

import com.faculty_evaluation_backend.fes.dto.audit.AuditLogResponse;
import com.faculty_evaluation_backend.fes.dto.audit.AuditLogSliceResponse;
import com.faculty_evaluation_backend.fes.dto.student.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public PageResponse<AuditLogResponse> findAuditLogs(
            Integer userId,
            String username,
            String action,
            String entityType,
            Instant startDate,
            Instant endDate,
            String search,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<AuditLogResponse> auditPage =
                auditLogRepository.findAuditLogs(
                                userId,
                                normalizeBlank(username),
                                normalizeBlank(action),
                                normalizeBlank(entityType),
                                startDate,
                                endDate,
                                normalizeBlank(search),
                                pageable
                        )
                        .map(AuditLogResponse::from);

        return PageResponse.<AuditLogResponse>builder()
                .content(auditPage.getContent())
                .totalElements(auditPage.getTotalElements())
                .totalPages(auditPage.getTotalPages())
                .page(auditPage.getNumber())
                .size(auditPage.getSize())
                .build();
    }

    @Transactional(
            transactionManager = "primaryTransactionManager",
            readOnly = true
    )
    public AuditLogSliceResponse findAuditLogSlice(
            Integer userId,
            String username,
            String action,
            String entityType,
            Instant startDate,
            Instant endDate,
            String search,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Slice<AuditLogResponse> auditSlice =
                auditLogRepository.findAuditLogSlice(
                                userId,
                                normalizeBlank(username),
                                normalizeBlank(action),
                                normalizeBlank(entityType),
                                startDate,
                                endDate,
                                normalizeBlank(search),
                                pageable
                        )
                        .map(AuditLogResponse::from);

        return AuditLogSliceResponse.from(auditSlice);
    }

    private String normalizeBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
