ALTER TABLE user_accounts
ADD COLUMN data_source VARCHAR(100);

UPDATE user_accounts ua

INNER JOIN primary_faculty pf
    ON LOWER(TRIM(pf.firstname)) = LOWER(TRIM(ua.firstname))
   AND LOWER(TRIM(pf.lastname)) = LOWER(TRIM(ua.lastname))

SET ua.data_source = pf.legacy_database;