/*
 * Production safety marker.
 *
 * These read indexes were originally created by this Flyway version, but
 * production runs MySQL 5.5 and large CREATE INDEX operations can exceed the
 * application startup socket timeout while locking/copying tables. Keep this
 * migration lightweight so deployment startup is not blocked by long DDL.
 *
 * Run docs/production-read-indexes-maintenance.sql manually during a database
 * maintenance window if the indexes are still needed.
 */
SELECT 1;
