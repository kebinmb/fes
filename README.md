# Faculty Evaluation System Backend

Spring Boot backend for the CHMSU Faculty Evaluation System. The application manages student, supervisor, and administrator evaluation workflows, faculty workload encoding, evidence uploads, audit logs, report verification, and legacy SIS data migration.

## Project Layout

```text
src/main/java/com/faculty_evaluation_backend/fes
├── audit/          Audit annotations, aspect, entities, and services
├── config/         Cache, datasource, JWT, OAuth2, CORS, and JSON config
├── controller/     Role and feature API entry points
├── dto/            Request, response, and projection models
├── entities/       JPA entities for primary, legacy, auth, and evaluation data
├── exceptions/     Shared API exceptions and global handler
├── migration/      Legacy-to-primary migration orchestration
├── repositories/   Spring Data repositories and native query projections
├── services/       Business logic by system area
└── utilities/      Shared helper classes
```

Resources:

```text
src/main/resources
├── application.yml
├── application-dev.yml
├── application-prod.yml
└── db/migration/   Flyway migrations
```

## Local Setup

1. Copy `.env.example` to `.env`.
2. Fill in local database, JWT, OAuth, mail, and Google Drive values.
3. Start MySQL databases expected by `application-dev.yml`.
4. Run:

```powershell
.\mvnw.cmd spring-boot:run
```

The default dev API runs at:

```text
http://localhost:8090/api
```

## Environment Rules

Do not commit secrets or generated files.

Ignored local/runtime files include:

```text
.env
.env.*
local-data/
tmp/
target/
src/main/resources/evidence-storage-account.json
src/main/resources/*service-account*.json
```

Only `.env.example` should be committed.

## Flyway Migrations

Migrations live in:

```text
src/main/resources/db/migration
```

Rules:

- Never edit a migration that has already run in production.
- Add a new migration for production changes.
- Use defensive `information_schema` checks for optional indexes or columns.
- Run `flyway repair` only after confirming a failed migration has been corrected and did not partially apply unsafe changes.

## Production Build

Package the backend with:

```powershell
.\mvnw.cmd -q -DskipTests package
```

Deploy:

```text
target/fes-0.0.1-SNAPSHOT.jar
```

After deployment, restart the production service so Spring loads the new jar and Flyway migrations.

## Testing

Run all tests:

```powershell
.\mvnw.cmd test
```

Use targeted tests before deploying changes in these areas:

- workload regular/overload filtering
- faculty evaluation print/report generation
- supervisor dashboards and evaluated-student queries
- admin dashboard counts
- session/refresh token behavior
- evidence upload validation

## Refactoring Notes

Service extraction guidance is documented in:

```text
docs/SERVICE_BOUNDARIES.md
```

Use it when splitting large services such as `AdministratorService` so transaction, cache, audit, and response behavior remain unchanged.
