package com.faculty_evaluation_backend.fes.entities.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LegacySubjectId implements Serializable {

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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LegacySubjectId that = (LegacySubjectId) object;
        return Objects.equals(subjectCode, that.subjectCode) &&
                Objects.equals(descriptiveTitle, that.descriptiveTitle) &&
                Objects.equals(lecUnits, that.lecUnits) &&
                Objects.equals(labUnits, that.labUnits) &&
                Objects.equals(lecHours, that.lecHours) &&
                Objects.equals(labHours, that.labHours);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectCode, descriptiveTitle, lecUnits, labUnits, lecHours, labHours);
    }
}
