package com.faculty_evaluation_backend.fes.dto.dashboard;

import lombok.Builder;

import java.util.List;

@Builder
public record AdminDashboardResponse(
        AdminDashboardSummaryResponse summary,
        List<AdminDashboardProgramBreakdownResponse> programs,
        List<AdminDashboardFacultyLoadResponse> facultyLoads
) {
}
