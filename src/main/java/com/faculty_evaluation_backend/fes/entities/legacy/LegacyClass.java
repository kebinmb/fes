package com.faculty_evaluation_backend.fes.entities.legacy;

import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacyClassId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "class")
public class LegacyClass {
    @EmbeddedId
    private LegacyClassId id;
    @Column(name = "faculty_id")
    private String facultyId;
    @Column(name = "subject_code")
    private String subjectCode;
}
