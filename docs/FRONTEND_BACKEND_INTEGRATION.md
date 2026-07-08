# Frontend and Backend Integration Guide

This document tracks the current backend API surface and how the Angular frontend should consume it.

## API Coverage

### Authentication

- `POST /auth/access-code/generate`
- `POST /auth/student/login`
- `POST /auth/supervisor/login`
- `POST /auth/administrator/login`
- `GET /auth/me`
- `PUT /auth/change-password`
- `POST /auth/logout`

Frontend owner: `AuthService`.

### Admin

- `GET /admin/dashboard`
- `GET /admin/dashboard/summary`
- `GET /admin/dashboard/programs`
- `GET /admin/dashboard/faculty-loads`
- `GET /admin/dashboard/faculty-workload-coverage`
- `GET /admin/faculties`
- `GET /admin/user-accounts`
- `GET /admin/faculty-evaluation-score`
- `PUT /admin/update-faculty`
- `GET /admin/faculty-workloads`
- `PUT /admin/faculty-workloads`
- `GET /admin/faculty-workloads/record`
- `GET /admin/faculty-workloads/{facultyWorkloadId}`
- `GET /admin/faculty-workloads/section-options`
- `GET /admin/faculty-workloads/class-options`
- `GET /admin/faculty-evaluation-score/{facultyId}`
- `POST /admin/faculty-evaluation-reports/{facultyId}`
- `PUT /admin/school-year-semester`
- `GET /admin/school-year-semester`
- `GET /admin/student-faculty-evaluation`
- `POST /admin/create-user`
- `PUT /admin/update-user`
- `PUT /admin/update-password`
- `GET /admin/student-sections`
- `GET /admin/student-evaluation-status`
- `GET /admin/audit-logs`
- `GET /admin/audit-logs/slice`

Frontend owner: `AdminService`.

### Faculty and Supervisor

- `GET /faculty/list`
- `GET /faculty/faculty-loads`
- `GET /faculty/faculty-classes`
- `GET /faculty/faculty-program-loads`
- `GET /faculty/check`
- `GET /faculty/evaluated-students`
- `GET /faculty/evidences/criteria`
- `POST /faculty/evidences`
- `GET /faculty/evidences/slice`
- `GET /faculty/evidences/{evidenceId}/download`
- `DELETE /faculty/evidences/{evidenceId}`

Frontend owners: `SupervisorDataService`, `FacultyEvidenceService`, and `EvaluationService`.

### Student

- `GET /student/student-loads`
- `GET /student/check`

Frontend owners: `StudentDataService` and `EvaluationService`.

The frontend should not call `/student/summary`, `/student/list`, or `/student/distribution` unless those endpoints are reintroduced in the backend.

### Evaluation

- `POST /evaluation/submit`
- `GET /verify-report/{reportId}`

Frontend owner: `EvaluationService`.

## Integration Rules

- Keep backend DTO names and frontend interfaces aligned field-by-field.
- Prefer backend-owned filtering, paging, and aggregation for large datasets.
- Keep frontend labels normalized for users, but send backend enum/database values over HTTP.
- For legacy database filters, display `Talisay`, `Alijis`, `Fortune-Towne`, and `Binalbagan`, but send `LEGACY_TALISAY`, `LEGACY_ALIJIS`, `LEGACY_FT`, and `LEGACY_BINALBAGAN`.
- Do not modify already-applied Flyway migrations. Add a new migration for any schema change.
- Any new backend endpoint should include a matching typed frontend service method before the feature is considered connected.

