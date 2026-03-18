package com.faculty_evaluation_backend.fes.entities.legacy;

import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacySectionId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "section")
@Entity
public class LegacySection {
    @EmbeddedId
    private LegacySectionId id;
    @Column(name = "section_code")
    private String sectionCode;
    @Column(name = "yearLevel")
    private String yearLevel;
}

