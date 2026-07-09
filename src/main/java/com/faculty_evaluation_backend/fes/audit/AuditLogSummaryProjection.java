package com.faculty_evaluation_backend.fes.audit;

import java.time.Instant;

public interface AuditLogSummaryProjection {
    Long getId();

    Integer getUserId();

    String getUsername();

    String getEntityType();

    Integer getEntityId();

    String getAction();

    String getIpAddress();

    String getUserAgent();

    String getRequestMethod();

    String getRequestPath();

    Long getExecutionTimeMs();

    String getOldValue();

    String getNewValue();

    Instant getCreatedAt();
}
