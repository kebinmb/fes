# Changelog

All notable backend changes should be documented in this file.

This project follows a simple semantic versioning style:

- `MAJOR` for breaking API, auth, or database-contract changes.
- `MINOR` for new backward-compatible endpoints or features.
- `PATCH` for bug fixes, optimizations, and documentation.

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

