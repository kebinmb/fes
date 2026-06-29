CREATE INDEX idx_evidence_faculty_created_at
    ON faculty_evaluation_evidence(faculty_id, created_at);

CREATE INDEX idx_evidence_faculty_context_created
    ON faculty_evaluation_evidence(faculty_id, school_year, semester, created_at);

CREATE INDEX idx_evidence_faculty_criterion_created
    ON faculty_evaluation_evidence(faculty_id, criterion, created_at);

CREATE INDEX idx_evidence_faculty_subject_created
    ON faculty_evaluation_evidence(faculty_id, class_code, subject_code, created_at);
