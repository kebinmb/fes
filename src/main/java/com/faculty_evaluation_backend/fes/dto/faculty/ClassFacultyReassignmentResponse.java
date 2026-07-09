package com.faculty_evaluation_backend.fes.dto.faculty;

import com.faculty_evaluation_backend.fes.audit.AuditableChange;

import java.util.LinkedHashMap;
import java.util.Map;

public record ClassFacultyReassignmentResponse(
        ClassFacultyAssignmentResponse assignment,
        String previousFacultyId,
        String newFacultyId,
        boolean changed
) implements AuditableChange {
    @Override
    public Long auditEntityId() {
        return assignment.primaryClassId();
    }

    @Override
    public Object auditOldValue() {
        return assignmentAuditValue(previousFacultyId, null);
    }

    @Override
    public Object auditNewValue() {
        return assignmentAuditValue(newFacultyId, changed);
    }

    private Map<String, Object> assignmentAuditValue(
            String facultyId,
            Boolean wasChanged
    ) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("primaryClassId", assignment.primaryClassId());
        value.put("classCode", assignment.classCode());
        value.put("facultyId", facultyId);
        if (wasChanged != null) {
            value.put("changed", wasChanged);
            value.put("status", "success");
        }
        return value;
    }
}
