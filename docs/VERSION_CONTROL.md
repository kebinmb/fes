# Version Control Guide

Use this as the release rhythm for the backend and frontend repositories.

## Branches

- `main`: stable, deployable code only.
- `develop`: optional integration branch for grouped features.
- `codex/<short-feature-name>`: implementation branches created during assisted work.
- `hotfix/<issue-name>`: urgent fixes based from `main`.

## Commit Style

Use short, scoped commits:

- `feat(admin): add faculty workload lookup by id`
- `fix(auth): normalize student access code input`
- `docs(release): add integration changelog`
- `refactor(frontend): remove unused student evaluation calls`

## Release Tags

Tag backend and frontend together when they are compatible:

- Backend tag: `backend-v0.1.0`
- Frontend tag: `frontend-v0.1.0`
- Coordinated release tag, if both repos are tagged together: `fes-v0.1.0`

## Release Checklist

1. Confirm backend tests pass.
2. Confirm frontend build passes.
3. Confirm the frontend service layer has methods for every newly used backend endpoint.
4. Confirm no frontend service calls point to removed backend endpoints.
5. Update both changelogs with high-level user-facing and technical changes.
6. Never edit production-applied Flyway migrations; create the next migration instead.
7. Commit backend and frontend changes separately with matching release notes.
8. Tag both repositories after verification.

## Suggested Current Version

Use `0.1.0` as the first coordinated integration baseline because the project now has connected admin dashboard, faculty workload, audit logs, evaluation, authentication, and migration-aware data population behavior.

