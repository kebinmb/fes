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
public class LegacyClassId implements Serializable {
    @Column(name = "class_code")
    private String classCode;
    @Column(name = "section_id")
    private Integer sectionId;
    @Column(name = "school_year")
    private Integer schoolYear;
    @Column(name = "semester")
    private String semester;
    @Override
    public boolean equals(Object object){
        if(this == object) return true;
        if(object == null || getClass() != object.getClass()) return false;
        LegacyClassId that = (LegacyClassId) object;
        return Objects.equals(classCode, that.classCode) &&
                Objects.equals(sectionId, that.sectionId) &&
                Objects.equals(schoolYear, that.schoolYear) &&
                Objects.equals(semester, that.semester);
    }
    @Override
    public int hashCode(){
        return Objects.hash(classCode, sectionId, schoolYear, semester);
    }
}
