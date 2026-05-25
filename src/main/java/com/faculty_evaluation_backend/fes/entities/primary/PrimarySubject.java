package com.faculty_evaluation_backend.fes.entities.primary;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import com.faculty_evaluation_backend.fes.migration.entity.MigratableEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "primary_subject")
@NoArgsConstructor
@AllArgsConstructor
public class PrimarySubject extends MigratableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "primary_subject_id")
    private Long primarySubjectId;

    @Column(name = "subject_code", unique = true)
    private String subjectCode;

    @Column(name = "descriptive_title")
    private String descriptiveTitle;
}
