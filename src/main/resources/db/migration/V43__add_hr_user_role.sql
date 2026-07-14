ALTER TABLE user_accounts
    MODIFY COLUMN user_role ENUM(
        'DEAN',
        'FACULTY',
        'PROGRAM_CHAIR',
        'STUDENT',
        'ADMIN',
        'HR'
    ) NOT NULL;
