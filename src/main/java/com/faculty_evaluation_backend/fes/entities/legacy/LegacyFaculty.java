package com.faculty_evaluation_backend.fes.entities.legacy;

import com.faculty_evaluation_backend.fes.entities.compositeKey.LegacyFacultyId;
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
@Table(name = "faculty")
@Entity
public class LegacyFaculty {
    @EmbeddedId
    private LegacyFacultyId id;
    @Column(name = "middlename")
    private String middlename;
    @Column(name = "position")
    private String position;
    @Column(name = "load_limit")
    private Double loadLimit;
}
