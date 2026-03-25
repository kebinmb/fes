package com.faculty_evaluation_backend.fes.entities.primary;

import com.faculty_evaluation_backend.fes.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "primary_section")
public class PrimarySection extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "primary_section_id")
    private Long primarySectionId;

    @Column(name = "section_id")
    private Integer sectionId;
    @Column(name = "year_level")
    private String yearLevel;
    @Column(name = "program_code")
    private String programCode;
    @Column(name = "section_code")
    private String sectionCode;
}
