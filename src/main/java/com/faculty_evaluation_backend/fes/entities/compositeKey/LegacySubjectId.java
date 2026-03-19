package com.faculty_evaluation_backend.fes.entities.compositeKey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LegacySubjectId {

    @Column(name = "subject_code")
    private String subjectCode;

    @Column(name = "descriptive_title")
    private String descriptiveTitle;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        LegacySubjectId that = (LegacySubjectId) object;
        return Objects.equals(subjectCode, that.subjectCode) &&
                Objects.equals(descriptiveTitle, that.descriptiveTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subjectCode, descriptiveTitle);
    }
}
