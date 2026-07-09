# Changelog

All notable backend changes should be documented in this file.

This project follows a simple semantic versioning style:

- `MAJOR` for breaking API, auth, or database-contract changes.
- `MINOR` for new backward-compatible endpoints or features.
- `PATCH` for bug fixes, optimizations, and documentation.

## [0.2.0] - 2026-07-09

### Added

- Added current-term admin endpoints for paginated class assignment browsing, active faculty options, and faculty reassignment.
- Added structured reassignment auditing with the affected `primaryClassId`, previous faculty ID, new faculty ID, administrator identity, request metadata, and execution time.
- Added optimized indexes for current-term class assignment and active faculty-option queries.

### Changed

- Class reassignment updates only `primary_class.faculty_id`; student loads, evaluation records, workload records, and migration provenance remain unchanged.
- Reassignment now validates that the class belongs to the active term and the selected faculty is active and belongs to the same source database.
- Added stale-edit protection to prevent one administrator from silently overwriting another administrator's newer assignment.
- Reassignment invalidates dependent dashboard, workload, faculty-class, student-load, and evaluation caches.
- Audit-log responses now expose structured `oldValue` and `newValue` details.

### Fixed

- Corrected the `V38` MySQL indexes to use bounded prefixes and remain below the InnoDB 3072-byte key limit under `utf8mb4`.

### Database Notes

- If an earlier `V38` attempt was recorded as failed, remove only the failed version-38 row from `flyway_schema_history` before restarting Flyway.
- The `V38` migration adds indexes only and does not modify populated application data.

## [0.1.0] - 2026-07-08

### Added

- Added faculty workload backend support for workload records, class options, section options, workload coverage, overload status, and workload-by-ID lookup.
- Added backend support for filtering admin faculty lists by `legacyDatabase`.
- Added backend faculty responses with `legacyDatabase` so the frontend can display campus/source labels.
- Added admin audit-log endpoints for paged and slice-based audit browsing.
- Added version-control and frontend/backend integration documentation.

### Changed

- Admin faculty listing excludes `FOR_MIGRATION` college records from normal dashboard population.
- Faculty access-code generation now uses a single-word access code contract.
- Faculty workload status is derived as `REGULAR_LOAD` or `OVERLOAD` from computed workload totals.

### Database Notes

- Existing Flyway migrations are production-sensitive. Do not edit migrations that have already been applied in shared or production-like databases.
- Additive schema work should use a new `V{next}__description.sql` migration.

