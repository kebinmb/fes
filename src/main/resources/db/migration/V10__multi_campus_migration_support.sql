-- =========================================================
-- ADD MIGRATION METADATA COLUMNS
-- =========================================================

ALTER TABLE primary_program
    ADD COLUMN legacy_database VARCHAR(255),
    ADD COLUMN legacy_id VARCHAR(255),
    ADD COLUMN source_campus ENUM(
        'LEGACY_ALIJIS',
        'LEGACY_BINALBAGAN',
        'LEGACY_FT',
        'LEGACY_TALISAY'
    );

ALTER TABLE primary_subject
    ADD COLUMN legacy_database VARCHAR(255),
    ADD COLUMN legacy_id VARCHAR(255),
    ADD COLUMN source_campus ENUM(
        'LEGACY_ALIJIS',
        'LEGACY_BINALBAGAN',
        'LEGACY_FT',
        'LEGACY_TALISAY'
    );

ALTER TABLE primary_faculty
    ADD COLUMN legacy_database VARCHAR(255),
    ADD COLUMN legacy_id VARCHAR(255),
    ADD COLUMN source_campus ENUM(
        'LEGACY_ALIJIS',
        'LEGACY_BINALBAGAN',
        'LEGACY_FT',
        'LEGACY_TALISAY'
    );

ALTER TABLE primary_student
    ADD COLUMN legacy_database VARCHAR(255),
    ADD COLUMN legacy_id VARCHAR(255),
    ADD COLUMN source_campus ENUM(
        'LEGACY_ALIJIS',
        'LEGACY_BINALBAGAN',
        'LEGACY_FT',
        'LEGACY_TALISAY'
    );

ALTER TABLE primary_section
    ADD COLUMN legacy_database VARCHAR(255),
    ADD COLUMN legacy_id VARCHAR(255),
    ADD COLUMN source_campus ENUM(
        'LEGACY_ALIJIS',
        'LEGACY_BINALBAGAN',
        'LEGACY_FT',
        'LEGACY_TALISAY'
    );

ALTER TABLE primary_class
    ADD COLUMN legacy_database VARCHAR(255),
    ADD COLUMN legacy_id VARCHAR(255),
    ADD COLUMN source_campus ENUM(
        'LEGACY_ALIJIS',
        'LEGACY_BINALBAGAN',
        'LEGACY_FT',
        'LEGACY_TALISAY'
    );

ALTER TABLE primary_student_load
    ADD COLUMN legacy_database VARCHAR(255),
    ADD COLUMN legacy_id VARCHAR(255),
    ADD COLUMN source_campus ENUM(
        'LEGACY_ALIJIS',
        'LEGACY_BINALBAGAN',
        'LEGACY_FT',
        'LEGACY_TALISAY'
    );

-- =========================================================
-- REMOVE OLD GLOBAL UNIQUE CONSTRAINTS
-- =========================================================

ALTER TABLE primary_subject
    DROP INDEX subject_code;

ALTER TABLE primary_student
    DROP INDEX student_id;

ALTER TABLE primary_faculty
    DROP INDEX faculty_id;

ALTER TABLE primary_section
    DROP INDEX section_id;

-- =========================================================
-- ADD NEW MULTI-CAMPUS UNIQUE CONSTRAINTS
-- =========================================================

ALTER TABLE primary_program
    ADD CONSTRAINT uk_program_legacy
        UNIQUE (legacy_database, legacy_id);

ALTER TABLE primary_subject
    ADD CONSTRAINT uk_subject_legacy
        UNIQUE (legacy_database, legacy_id);

ALTER TABLE primary_faculty
    ADD CONSTRAINT uk_faculty_legacy
        UNIQUE (legacy_database, legacy_id);

ALTER TABLE primary_student
    ADD CONSTRAINT uk_student_legacy
        UNIQUE (legacy_database, legacy_id);

ALTER TABLE primary_section
    ADD CONSTRAINT uk_section_legacy
        UNIQUE (legacy_database, legacy_id);

ALTER TABLE primary_class
    ADD CONSTRAINT uk_class_legacy
        UNIQUE (legacy_database, legacy_id);

ALTER TABLE primary_student_load
    ADD CONSTRAINT uk_student_load_legacy
        UNIQUE (legacy_database, legacy_id);

-- =========================================================
-- REMOVE FK CONSTRAINTS
-- =========================================================

ALTER TABLE primary_class
    DROP FOREIGN KEY fk_class_faculty;

ALTER TABLE primary_class
    DROP FOREIGN KEY fk_class_subject;

ALTER TABLE primary_class
    DROP FOREIGN KEY fk_class_section;

ALTER TABLE primary_student_load
    DROP FOREIGN KEY fk_student_load;

-- =========================================================
-- ADD MULTI-CAMPUS INDEXES
-- =========================================================

CREATE INDEX idx_program_legacy
    ON primary_program(legacy_database, legacy_id);

CREATE INDEX idx_subject_legacy
    ON primary_subject(legacy_database, legacy_id);

CREATE INDEX idx_faculty_legacy
    ON primary_faculty(legacy_database, legacy_id);

CREATE INDEX idx_student_legacy
    ON primary_student(legacy_database, legacy_id);

CREATE INDEX idx_section_legacy
    ON primary_section(legacy_database, legacy_id);

CREATE INDEX idx_class_legacy
    ON primary_class(legacy_database, legacy_id);

CREATE INDEX idx_student_load_legacy
    ON primary_student_load(legacy_database, legacy_id);
