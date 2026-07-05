ALTER TABLE faculty_workload
    ADD COLUMN lecture_hours_per_week DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN laboratory_hours_per_week DECIMAL(8, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN total_hours_per_week DECIMAL(8, 2) NOT NULL DEFAULT 0.00;

UPDATE faculty_workload
SET total_hours_per_week = COALESCE(total_teaching_load, 0.00)
WHERE total_hours_per_week = 0.00;
