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
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "primary_program")
public class PrimaryProgram extends MigratableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "primary_program_id")
    private Long primaryProgramId;

    @Column(name = "program_code")
    private String programCode;
    @Column(name = "program_title")
    private String programTitle;
    @Column(name = "year_granted")
    private Integer yearGranted;
    @Column(name = "college_code")
    private String collegeCode;
}
