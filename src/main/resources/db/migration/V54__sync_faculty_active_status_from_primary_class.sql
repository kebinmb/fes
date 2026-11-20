ALTER TABLE primary_faculty
    ADD COLUMN campus VARCHAR(255) NULL AFTER status;

-- Synchronize active status for faculty who have assigned classes in primary_class, matching by campus (legacy_database)
UPDATE primary_faculty pf
INNER JOIN primary_class pc
    ON pf.faculty_id = pc.faculty_id
   AND pf.legacy_database = pc.legacy_database
SET pf.status = 'ACTIVE';

-- Update campus column only for active faculty based on legacy_database
UPDATE primary_faculty
SET campus = CASE
    WHEN legacy_database = 'LEGACY_TALISAY' THEN 'TALISAY'
    WHEN legacy_database = 'LEGACY_ALIJIS' THEN 'ALIJIS'
    WHEN legacy_database = 'LEGACY_BINALBAGAN' THEN 'BINALBAGAN'
    WHEN legacy_database = 'LEGACY_FT' THEN 'FT'
    ELSE NULL
END
WHERE status = 'ACTIVE';
