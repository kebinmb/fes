package com.faculty_evaluation_backend.fes.entities.legacy;

import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacyProgramId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "program")
public class LegacyProgram {
    @EmbeddedId
    private LegacyProgramId id;
    @Column(name = "year_granted")
    private Integer yearGranted;
    @Column(name = "college_code")
    private String collegeCode;
}
