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

    @Column(name = "subject_code")
    private String subjectCode;

    @Column(name = "descriptive_title")
    private String descriptiveTitle;

    @Column(name = "lec_units")
    private Double lecUnits;

    @Column(name = "lab_units")
    private Double labUnits;

    @Column(name = "lec_hours")
    private Double lecHours;

    @Column(name = "lab_hours")
    private Double labHours;
}
