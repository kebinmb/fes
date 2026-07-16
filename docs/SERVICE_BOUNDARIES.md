# Service Boundary Guide

The current runtime service wiring is intentionally preserved. Use this guide when extracting large services into smaller units so behavior remains stable.

## Current Hotspots

The largest service classes are:

- `AdministratorService`
- `EvaluationDataService`
- `EvidenceStorageService`
- `SupervisorDataService`

`AdministratorService` is the highest-priority extraction candidate because it contains unrelated workflows in one class.

## Recommended Admin Service Split

Extract incrementally in this order:

1. `AdminDashboardService`
   - dashboard summary
   - program breakdown
   - top faculty loads
   - workload coverage
   - supervisor evaluation dashboard

2. `AdminFacultyManagementService`
   - faculty list/search
   - faculty update
   - class faculty reassignment
   - faculty assignment options

3. `AdminWorkloadService`
   - workload encoding
   - workload class options
   - workload validation
   - load limit and overload calculations

4. `AdminUserAccountService`
   - account creation
   - account update
   - password changes
   - role and status validation

5. `AdminStudentEvaluationStatusService`
   - student section evaluation list
   - student evaluation status print/detail data

## Refactor Rules

- Keep controller endpoints and response DTOs unchanged.
- Move one method group at a time.
- Add or update tests before each extraction.
- Keep repository method signatures unchanged unless the same change is covered by tests.
- Avoid moving audit annotations from controller methods unless behavior is explicitly verified.
- Preserve cache names and cache keys during extraction.
- Run `mvn test` after every extraction step.

## Why Not Split Everything At Once

Large service extraction can easily alter transaction boundaries, cache behavior, audit coverage, and exception mapping. The safer approach is to first add regression tests around current behavior, then move one cohesive workflow per change.

