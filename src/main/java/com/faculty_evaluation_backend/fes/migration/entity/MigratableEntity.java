package com.faculty_evaluation_backend.fes.migration.entity;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.config.database.LegacyDatabase;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class MigratableEntity extends Auditable {

    @Enumerated(EnumType.STRING)
    @Column(name = "source_campus")
    private LegacyDatabase sourceCampus;

    @Column(name = "legacy_database")
    private String legacyDatabase;

    @Column(name = "legacy_id")
    private String legacyId;
}