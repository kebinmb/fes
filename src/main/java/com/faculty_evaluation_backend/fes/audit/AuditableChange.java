package com.faculty_evaluation_backend.fes.audit;

public interface AuditableChange {
    Long auditEntityId();

    Object auditOldValue();

    Object auditNewValue();
}
