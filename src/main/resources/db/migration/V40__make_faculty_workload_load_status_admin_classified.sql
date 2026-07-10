UPDATE faculty_workload
SET load_status = CASE
    WHEN UPPER(TRIM(load_status)) IN ('OVERLOAD') THEN 'Overload'
    ELSE 'Regular'
END;

ALTER TABLE faculty_workload
    MODIFY load_status VARCHAR(30) NOT NULL DEFAULT 'Regular';

ALTER TABLE faculty_workload
    ADD CONSTRAINT chk_faculty_workload_load_status
        CHECK (load_status IN ('Regular', 'Overload'));
