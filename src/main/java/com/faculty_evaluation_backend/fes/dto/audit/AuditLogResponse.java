package com.faculty_evaluation_backend.fes.dto.audit;

import com.faculty_evaluation_backend.fes.audit.AuditLog;
import com.faculty_evaluation_backend.fes.audit.AuditLogSummaryProjection;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Integer userId,
        String username,
        String entityType,
        Integer entityId,
        String action,
        String ipAddress,
        String userAgent,
        String requestMethod,
        String requestPath,
        Long executionTimeMs,
        String oldValue,
        String newValue,
        String status,
        Instant createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                log.getUsername(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getRequestMethod(),
                log.getRequestPath(),
                log.getExecutionTimeMs(),
                log.getOldValue(),
                log.getNewValue(),
                resolveStatus(log.getAction()),
                log.getCreatedAt()
        );
    }

    public static AuditLogResponse from(AuditLogSummaryProjection log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                log.getUsername(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getRequestMethod(),
                log.getRequestPath(),
                log.getExecutionTimeMs(),
                log.getOldValue(),
                log.getNewValue(),
                resolveStatus(log.getAction()),
                log.getCreatedAt()
        );
    }

    private static String resolveStatus(String action) {
        if (action == null) {
            return "UNKNOWN";
        }

        if (action.endsWith("_SUCCESS")) {
            return "SUCCESS";
        }

        if (action.endsWith("_FAILED")) {
            return "FAILED";
        }

        return "RECORDED";
    }
}
